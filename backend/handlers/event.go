package handlers

import (
	"encoding/json"
	"net/http"

	"cloud.google.com/go/firestore"
	"github.com/dbc/models"
	"github.com/gorilla/mux"
	"github.com/rs/zerolog/log"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"
)

func Event(w http.ResponseWriter, r *http.Request) {
  vars := mux.Vars(r)

  eventID, ok := vars["event"]
  if !ok && r.Method != "POST" {
    msg := "No event ID supplied."
		log.Error().Msg(msg)
		http.Error(w, msg, http.StatusBadRequest)
		return
  }

	client := r.Context().Value("firestore").(*firestore.Client)

  var err error
  var dsnap *firestore.DocumentSnapshot

  switch r.Method {
  case "POST":
    fallthrough
  case "PUT":
    var body models.Event
    err = json.NewDecoder(r.Body).Decode(&body)
    if err != nil {
      break
    }
    _, err = client.Collection("events").Doc(body.ID).Create(r.Context(), body)
    if err != nil { break }

  case "GET":
    dsnap, err = client.Collection("events").Doc(eventID).Get(r.Context())
		if err != nil { break }

		jsonData, err := json.Marshal(dsnap.Data())
		if err != nil { break }

		var event models.Event
		err = json.Unmarshal(jsonData, &event)
		if err != nil { break }

		str, err := json.Marshal(event)
		if err != nil { break }

		w.Header().Set("Content-Type", "application/json")
		w.Write([]byte(str))
    return

  case "DELETE":
    _, err = client.Collection("events").Doc(eventID).Delete(r.Context())
  }
  if err != nil {
    log.Err(err)
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
  }
}

type EventExistsResponse struct {
  Exists bool `json:"exists"`
}

// For checking if an event with an ID exists
func EventExists(w http.ResponseWriter, r *http.Request) {
  vars := mux.Vars(r)

  eventID, ok := vars["event"]
  if !ok && r.Method != "POST" {
    msg := "No event ID supplied."
		log.Error().Msg(msg)
		http.Error(w, msg, http.StatusBadRequest)
		return
  }

  exists := EventExistsResponse{Exists: true}

	client := r.Context().Value("firestore").(*firestore.Client)

  _, err := client.Collection("events").Doc(eventID).Get(r.Context())
  if status.Code(err) == codes.NotFound {
    exists.Exists = false
  } else if err != nil {
    log.Err(err)
    http.Error(w, err.Error(), http.StatusInternalServerError)
    return
  }

  resp, err := json.Marshal(EventExistsResponse{Exists: true})
  if err != nil {
    log.Err(err)
    http.Error(w, err.Error(), http.StatusInternalServerError)
    return
  }

  w.Header().Set("Content-Type", "application/json")
  w.Write(resp)
}

func EventCards(w http.ResponseWriter, r *http.Request) {
  vars := mux.Vars(r)

  eventID, ok := vars["event"]
  if !ok && r.Method != "POST" {
    msg := "No event ID supplied."
		log.Error().Msg(msg)
		http.Error(w, msg, http.StatusBadRequest)
		return
  }

  cardID, ok := vars["card"]
  if !ok && r.Method != "DELETE" {
    msg := "No card ID supplied."
		log.Error().Msg(msg)
		http.Error(w, msg, http.StatusBadRequest)
		return
  }

	client := r.Context().Value("firestore").(*firestore.Client)

  _, err := client.Collection("events").Doc(eventID).Get(r.Context())
  if status.Code(err) == codes.NotFound {
    msg := "Event with supplied ID does not exist."
		log.Error().Msg(msg)
		http.Error(w, msg, http.StatusBadRequest)
		return
  } else if err != nil {
    log.Err(err)
    http.Error(w, err.Error(), http.StatusInternalServerError)
    return
  }

  CRUD: switch r.Method {
  case "POST":
    fallthrough
  case "PUT":
    var body models.BusinessCard
    err = json.NewDecoder(r.Body).Decode(&body)
    if err != nil { break }

    ref := client.Collection("events").Doc(eventID).Collection("cards").NewDoc()
    body.ID = ref.ID
    _, err := ref.Set(r.Context(), body)
    if err != nil { break }

    w.Write([]byte(body.ID))
    return

  case "GET":
    docs, err := client.Collection("events").Doc(eventID).Collection("cards").Documents(r.Context()).GetAll()
    if err != nil { break }
    cards := make([]models.BusinessCard, len(docs))
    for i, doc := range docs {
      jsonData, err := json.Marshal(doc.Data())
      if err != nil { break CRUD }

      var card models.BusinessCard
      err = json.Unmarshal(jsonData, &card)
      if err != nil { break CRUD }

      cards[i] = card
    }
    resp, err := json.Marshal(cards)
    if err != nil { break }

    w.Header().Set("Content-Type", "application/json")
    w.Write(resp)
    return

  case "DELETE":
    _, err = client.Collection("events").Doc(eventID).Collection("cards").Doc(cardID).Delete(r.Context())
  }
  if err != nil {
    log.Err(err)
    http.Error(w, err.Error(), http.StatusInternalServerError)
    return
  }
}
