package handlers

import (
	"encoding/json"
	"net/http"

	"cloud.google.com/go/firestore"
	"github.com/dbc/models"
	"github.com/gorilla/mux"
  "github.com/gorilla/websocket"
	"github.com/rs/zerolog/log"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"
)

func Event(w http.ResponseWriter, r *http.Request) {
  vars := mux.Vars(r)

  eventID, ok := vars["event"]
  if !ok && (r.Method != "POST") {
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
    var body models.Event
    err = json.NewDecoder(r.Body).Decode(&body)
    if err != nil {
      break
    }

    ref := client.Collection("events").NewDoc()
    _, err = ref.Create(r.Context(), body)
    if err != nil { break }

    body.ID = ref.ID

		str, err := json.Marshal(body)
		if err != nil { break }

		w.Header().Set("Content-Type", "application/json")
		w.Write([]byte(str))
    return

  case "PUT":
    var body models.Event
    err = json.NewDecoder(r.Body).Decode(&body)
    if err != nil {
      break
    }

    _, err = client.Collection("events").Doc(eventID).Set(r.Context(), body)
    if err != nil { break }
    return

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

// For checking if an event witaaaaaaaah an ID exists
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

  resp, err := json.Marshal(exists)
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

var upgrader = websocket.Upgrader{
  // This is to get around some origin checking. 
  // API key authentication is enough security for this use case.
  CheckOrigin: func (_ *http.Request) bool { return true },
}

var eventConnMap = map[string]map[*websocket.Conn]bool{}

func ConnectEvent(w http.ResponseWriter, r *http.Request) {
  vars := mux.Vars(r)

  eventID, ok := vars["event"]
  if !ok {
    msg := "No event ID supplied."
		log.Error().Msg(msg)
		http.Error(w, msg, http.StatusBadRequest)
		return
  }

  // TODO: Check event exists before upgrading connection.

  conn, err := upgrader.Upgrade(w, r, nil)

  if err != nil {
    msg := "Failed to upgrade connection to websocket: " + err.Error()
    log.Error().Msg(msg)
    http.Error(w, msg, http.StatusInternalServerError)
  }
  log.Info().Msg("Opened Connection")
  if eventConnMap[eventID] == nil {
    eventConnMap[eventID] = map[*websocket.Conn]bool{}
  }
  eventConnMap[eventID][conn] = true
  defer func() {
    log.Info().Msg("Closing connection...")
    delete(eventConnMap[eventID], conn)
    if len(eventConnMap[eventID]) == 0 {
      delete(eventConnMap, eventID)
    }
    conn.Close()
  }()

  for {
    messageType, message, err := conn.ReadMessage()
    if err != nil {
      log.Error().Msg("Failed to read message: " + err.Error())
      break
    }

    switch messageType {
    case websocket.TextMessage:     
      for c := range eventConnMap[eventID] {
        if c != conn {
          err := c.WriteMessage(websocket.TextMessage, message)
          if err != nil {
            msg := "Failed to write message: " + err.Error()
            log.Error().Msg(msg)
            http.Error(w, msg, http.StatusInternalServerError)
            break
          }
        }
      }
    default: return
    }
  }
}
