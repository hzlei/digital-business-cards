package handlers

import (
	"io"
	"net/http"

	"cloud.google.com/go/firestore"
)

var Client *firestore.Client

func IndexHandler(w http.ResponseWriter, r *http.Request) {
   w.WriteHeader(http.StatusOK)
   io.WriteString(w, "Working!")
}
