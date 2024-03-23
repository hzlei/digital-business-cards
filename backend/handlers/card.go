package handlers

import (
	"encoding/json"
	"net/http"

	models "github.com/dbc/models"
	"github.com/rs/zerolog/log"
)


type AddCardBody struct {
  User string `json:"user"`
  Card models.BusinessCard `json:"card"`
}

func AddCardHandler(w http.ResponseWriter, r *http.Request) {
  var body AddCardBody
  err := json.NewDecoder(r.Body).Decode(&body)
  if err != nil {
    log.Err(err)
    http.Error(w, err.Error(), http.StatusBadRequest)
  }
  
  Client.Collection("cards")
}
