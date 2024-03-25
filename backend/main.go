package main

import (
	"context"
	"fmt"
	"log"
	"net/http"

	"cloud.google.com/go/firestore"
	firebase "firebase.google.com/go"
	"github.com/gorilla/mux"
	"github.com/joho/godotenv"
	"google.golang.org/api/option"
)

const (
  firebaseConfigFile = "firebaseConfig.json"
)

func main() {
  err := godotenv.Load(".env")
  if err != nil {
    log.Fatal("Error loading .env file")
  }

  // Initialize Firebase
  opt := option.WithCredentialsFile("serviceAccountKey.json")
  ctx := context.Background()
  app, err := firebase.NewApp(ctx, nil, opt)
  if err != nil {
    log.Fatalln(err)
  }

  client, err := app.Firestore(ctx)
  if err != nil {
    log.Fatalln(err)
  }
  defer client.Close()

  // Initialize Router
  router := mux.NewRouter()

  // Define API routes
  // router.HandleFunc("/api/event", createEventHandler).Methods("POST")
  // router.HandleFunc("/api/event/{id}", eventHandler).Methods("GET", "PUT", "DELETE")

  router.HandleFunc("/api/card", addCardHandler(client)).Methods("POST")
  // router.HandleFunc("/api/card/{id}", cardHandler).Methods("GET", "PUT", "DELETE")

  // router.HandleFunc("/api/sendRequest", createSendRequestHandler).Methods("POST")
  // router.HandleFunc("/api/sendRequest/{id}", sendRequestHandler).Methods("GET", "PUT", "DELETE")
  //
  // router.HandleFunc("/api/retrieveRequest", recieveRequestHandler).Methods("POST")
  // router.HandleFunc("/api/retrieveRequest/{id}", recieveRequestHandler).Methods("GET", "PUT", "DELETE")

  // Start the API server
  port := ":8080"
  fmt.Printf("Server is running on port %s...\n", port)
  log.Fatal(http.ListenAndServe(port, router))
}

func addCardHandler(client *firestore.Client) func (w http.ResponseWriter, r *http.Request) {
  return nil
}
