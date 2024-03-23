package main

import (
	"context"
	"flag"
	"fmt"
	"net/http"
	"os"
	"os/signal"
	"time"

	"cloud.google.com/go/firestore"
	firebase "firebase.google.com/go"
	"github.com/gorilla/mux"
	"github.com/joho/godotenv"
	"github.com/rs/zerolog"
	"github.com/rs/zerolog/log"
	"google.golang.org/api/option"

	handlers "github.com/dbc/handlers"
)

var (
  Client *firestore.Client
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
  app, err := firebase.NewApp(ctx, nil, opt)
  if err != nil {
    log.Err(err)
  }

  Client, err := app.Firestore(ctx)
  if err != nil {
    log.Err(err)
  }
  defer Client.Close()

  // Initialize Router
  router := mux.NewRouter()

  // Define API routes

  router.HandleFunc("/", handlers.IndexHandler).Methods("GET")

  router.HandleFunc("/user", handlers.CreateUserIdHandler).Methods("POST")

  // router.HandleFunc("/api/event", createEventHandler).Methods("POST")
  // router.HandleFunc("/api/event/{id}", eventHandler).Methods("GET", "PUT", "DELETE")

  router.HandleFunc("/api/card", handlers.AddCardHandler).Methods("POST")
  // router.HandleFunc("/api/card/{id}", cardHandler).Methods("GET", "PUT", "DELETE")

  // router.HandleFunc("/api/sendRequest", createSendRequestHandler).Methods("POST")
  // router.HandleFunc("/api/sendRequest/{id}", sendRequestHandler).Methods("GET", "PUT", "DELETE")
  //
  // router.HandleFunc("/api/retrieveRequest", recieveRequestHandler).Methods("POST")
  // router.HandleFunc("/api/retrieveRequest/{id}", recieveRequestHandler).Methods("GET", "PUT", "DELETE")

  // Middleware
  router.Use(loggingMiddleware)
  router.Use(apiKeyAuthMiddleware)

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
