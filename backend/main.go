package main

import (
	"context"
	"flag"
	"fmt"
	"net/http"
	"os"
	"os/signal"
	"time"

	firebase "firebase.google.com/go"
	"github.com/gorilla/mux"
	"github.com/joho/godotenv"
	"github.com/rs/zerolog"
	"github.com/rs/zerolog/log"
	"google.golang.org/api/option"

	handlers "github.com/dbc/handlers"
	middlewares "github.com/dbc/middlewares"
)

func main() {
  // flag(s) parsing
  var wait time.Duration
  flag.DurationVar(&wait, "graceful-timeout", time.Second * 15, "the duration to wait for existing connections to finish. (defaults to 15s)")
  flag.Parse()

  // Logger setup
  log.Logger = log.Output(zerolog.ConsoleWriter{Out: os.Stderr})

  err := godotenv.Load(".env")
  if err != nil {
    log.Info().Msg("Error loading .env file")
  }

  // Initialize Firebase
  opt := option.WithCredentialsFile("serviceAccountKey.json")
  ctx := context.Background()
  config := &firebase.Config{
    StorageBucket: "digital-business-cards-573d8.appspot.com",
  }
  app, err := firebase.NewApp(ctx, config, opt)
  if err != nil {
    log.Err(err)
  }

  firestore, err := app.Firestore(ctx)
  if err != nil {
    log.Err(err)
  }
  defer firestore.Close()
  log.Info().Msg("Firebase firestore client loaded successfully")
  storage, err := app.Storage(ctx)
  if err != nil {
    log.Err(err)
  }
  log.Info().Msg("Firebase storage client loaded successfully")

  // Initialize Router
  router := mux.NewRouter()

  // Define API routes
  router.HandleFunc("/api", handlers.Index).Methods("GET")

  router.HandleFunc("/api/user", handlers.CreateUserId).Methods("POST")
  router.HandleFunc("/api/user/{user}", handlers.CreateUserId).Methods("GET", "DELETE")

  router.HandleFunc("/api/event", handlers.Event).Methods("POST", "PUT")
  router.HandleFunc("/api/event/{event}", handlers.Event).Methods("GET", "DELETE")
  router.HandleFunc("/api/event/{event}/exists", handlers.EventExists).Methods("GET")
  router.HandleFunc("/api/event/{event}/card", handlers.EventCards).Methods("POST", "PUT", "GET")
  router.HandleFunc("/api/event/{event}/card/{card}", handlers.EventCards).Methods("DELETE")
  router.HandleFunc("/api/event/{event}/socket", handlers.ConnectEvent)

  router.HandleFunc("/api/user/{user}/card", handlers.Card).Methods("POST", "PUT")
  router.HandleFunc("/api/user/{user}/card/{card}", handlers.Card).Methods("GET", "DELETE")
  router.HandleFunc("/api/user/{user}/card/{card}/image/{side}", handlers.CardImage).Methods("POST", "GET")

  // Middleware
  router.Use(middlewares.Logging)
  router.Use(middlewares.APIKey)
  fwm := &middlewares.Firebase{
    FirestoreClient: firestore,
    StorageClient: storage,
  }
  router.Use(fwm.LoadContext)

  // Start the API server (with graceful shutdown)
  srv := &http.Server{
    Addr: "0.0.0.0:8080",
    Handler: router,
  }
  log.Info().Msg(fmt.Sprintf("Server is running on address %s...\n", srv.Addr))

  // Run in go routine to avoid blocking
  go func() {
    if err := srv.ListenAndServe(); err != nil {
      log.Err(err)
    }
  }()

  c := make(chan os.Signal, 1)
  signal.Notify(c, os.Interrupt)

  <-c

  ctx, cancel := context.WithTimeout(context.Background(), wait)
  defer cancel()

  srv.Shutdown(ctx)

  log.Info().Msg("Shutting down...")
  os.Exit(0)
}
