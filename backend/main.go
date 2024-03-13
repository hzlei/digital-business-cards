package main

import (
  "context"
  "fmt"
  "log"
  "net/http"

  "firebase.google.com/go"
  "firebase.google.com/go/db"
  "github.com/gorilla/mux"
  "google.goland.org/api/option"
)

const (
  firebaseConfigFile = "path/to/our/firebaseConfig.json"
  firebaseDbUrl = "https://our-firebase-project.firebaseio.com"
)

var (
  ctx context.Context
  app *firebase.App
)

func main() {
  // Initialize Firebase
  ctx = context.Background()
  opt := option.WithCredentialsFile(firebaseConfigFile)
  app, err := firebase.NewApp(ctx, nil, opt)
  if err != nil {
    log.Fatalf("Firebase initialization error: %v\n", err)
  }

  // Initialize Firestore
  client, err := app.DatabaseWithURL(ctx, firebaseDbUrl)
  if err != nil {
    log.Fatalf("Firestore initialization error: %v\n", err)
  }

  // Initialize Router
  router := mux.NewRouter()

  // Define API routes
  router.HandleFunc("/api/createEvent", createEvents).Methods("POST")
  router.HandleFunc("/api/getEvent", getEvent).Methods("GET")
  router.HandleFunc("/api/joinEvent/{id}", joinEvent).Methods("PUT")
  router.HandleFunc("/api/sendCard", sendCard).Methods("POST")
  router.HandleFunc("/api/getCard/{id}", getCard).Methods("GET")
  router.HandleFunc("/api/updateCard/{id}", updateCard).Methods("PUT")
  router.HandleFunc("/api/deleteEvent/{id}", deleteEvent).Methods("DELETE")
  router.HandleFunc("/api/deleteUser/{id}", deleteCard).Methods("DELETE")
  router.HandleFunc("/api/postSendRequest", postSendRequest).Methods("POST")
  router.HandleFunc("/api/postGetRequest", postGetRequest).Methods("POST")
  router.HandleFunc("/api/deleteSendRequest", deleteSendRequest).Methods("DELETE")
  router.HandleFunc("/api/deleteGetRequest", deleteGetRequest).Methods("DELETE")
  router.HandleFunc("/api/getSendRequest", getSendRequest).Methods("GET")
  router.HandleFunc("/api/getGetRequest", getGetRequest).Methods("GET")

}
