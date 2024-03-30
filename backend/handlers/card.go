package handlers

import (
	"context"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"time"

	"cloud.google.com/go/firestore"
	"cloud.google.com/go/storage"
	firebaseStorage "firebase.google.com/go/storage"
	"github.com/gorilla/mux"
	"github.com/rs/zerolog/log"

	models "github.com/dbc/models"
)

func Card(w http.ResponseWriter, r *http.Request) {

	vars := mux.Vars(r)

	userID, ok := vars["user"]
	if !ok {
		msg := "No user ID supplied."
		log.Error().Msg(msg)
		http.Error(w, msg, http.StatusBadRequest)
		return
	}

	cardID, ok := vars["card"]
	if !ok && r.Method != "POST" {
		msg := "No card ID supplied."
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
		var body models.BusinessCard
		err = json.NewDecoder(r.Body).Decode(&body)
		if err != nil {
			break
		}
		_, err = client.Collection("users").Doc(userID).Collection("cards").Doc(body.ID).Set(r.Context(), body)

	case "GET":
		dsnap, err = client.Collection("users").Doc(userID).Collection("cards").Doc(cardID).Get(r.Context())
		if err != nil { break }

		jsonData, err := json.Marshal(dsnap.Data())
		if err != nil { break }

		var card models.BusinessCard
		err = json.Unmarshal(jsonData, &card)
		if err != nil { break }

		resp, err := json.Marshal(card)
		if err != nil { break }

		w.Header().Set("Content-Type", "application/json")
		w.Write(resp)
    return

	case "DELETE":
		_, err = client.Collection("users").Doc(userID).Collection("cards").Doc(cardID).Delete(r.Context())
	}
	if err != nil {
		log.Err(err)
		http.Error(w, err.Error(), http.StatusInternalServerError)
    return
	}
}

// Takes in a user ID and card ID so that the corresponding card gets updated.
func CardImage(w http.ResponseWriter, r *http.Request) {

	vars := mux.Vars(r)

	userID, ok := vars["user"]
	if !ok {
		msg := "No user ID supplied."
		log.Error().Msg(msg)
		http.Error(w, msg, http.StatusBadRequest)
		return
	}

	cardID, ok := vars["card"]
	if !ok && r.Method != "POST" {
		msg := "No card ID supplied."
		log.Error().Msg(msg)
		http.Error(w, msg, http.StatusBadRequest)
		return
	}

	side, ok := vars["side"]
	if !ok || (side != "front" && side != "back") {
		msg := "No side (either \"front\" or \"back\") supplied."
		log.Error().Msg(msg)
		http.Error(w, msg, http.StatusBadRequest)
		return
	}

	client := r.Context().Value("firestore").(*firestore.Client)

	// Retrieve card
	dsnap, err := client.Collection("users").Doc(userID).Collection("cards").Doc(cardID).Get(r.Context())
	if err != nil {
		log.Err(err)
		http.Error(w, err.Error(), http.StatusBadRequest)
		return
	}

	jsonData, err := json.Marshal(dsnap.Data())
	if err != nil {
		log.Err(err)
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	var card models.BusinessCard
	if err = json.Unmarshal(jsonData, &card); err != nil {
		log.Err(err)
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	storageClient := r.Context().Value("storage").(*firebaseStorage.Client)

	bucket, err := storageClient.DefaultBucket()

   name := fmt.Sprintf("%s:%s:%s", userID, cardID, side)

	switch r.Method {
	case "POST":
    // Set max size
    var max_size_mb int64 = 32
    err := r.ParseMultipartForm(max_size_mb << 20)
    if err != nil {
      log.Err(err)
      http.Error(w, err.Error(), http.StatusBadRequest)
      return
    }

		// Get image
		f, _, err := r.FormFile("image")
		if err != nil {
			log.Err(err)
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}
		defer f.Close()

		ctx := context.Background()
		ctx, cancel := context.WithTimeout(ctx, time.Second*50)
		defer cancel()

		obj := bucket.Object(name)

		// Perform upload
		wc := obj.NewWriter(ctx)
		if _, err = io.Copy(wc, f); err != nil {
			msg := "Failed to copy image to storage bucket"
			log.Error().Msg(msg)
			http.Error(w, msg, http.StatusInternalServerError)
		}
		if err := wc.Close(); err != nil {
			msg := "Failed to close storage object writer."
			log.Error().Msg(msg)
			http.Error(w, msg, http.StatusInternalServerError)
			return
		}
		log.Info().Msg(fmt.Sprint("Finished uploading file: ", name))

		// Update card
		switch side {
		case "front":
			card.Front = name
		case "back":
			card.Back = name
		}
		_, err = client.Collection("users").Doc(userID).Collection("cards").Doc(cardID).Set(r.Context(), card)
		if err != nil {
			log.Err(err)
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}

    // Return it back
    str, err := json.Marshal(card)
    if err != nil {
			log.Err(err)
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
    }
    w.Header().Set("Content-Type", "application/json")
    w.Write(str)

	case "GET":
		ctx := context.Background()
		ctx, cancel := context.WithTimeout(ctx, time.Second*50)
		defer cancel()

		if err != nil {
			log.Err(err)
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}

		rc, err := bucket.Object(name).NewReader(ctx)
		if err == storage.ErrObjectNotExist {
			msg := "Failed to find object."
			log.Error().Msg(msg)
			http.Error(w, msg, http.StatusNotFound)
			return
		} else if err != nil {
			log.Err(err)
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}
		defer rc.Close()

		data, err := io.ReadAll(rc)
		if err != nil {
			log.Err(err)
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}

		_, err = w.Write(data)
		if err != nil {
			log.Err(err)
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}
	}
}
