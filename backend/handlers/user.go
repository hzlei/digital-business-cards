package handlers

import (
	"net/http"

	"cloud.google.com/go/firestore"
	"github.com/rs/zerolog/log"
)

func CreateUserId(w http.ResponseWriter, r *http.Request) {
   client := r.Context().Value("firestore").(*firestore.Client)
   ref := client.Collection("users").NewDoc()
   data := make(map[string]interface{})
   _, err := ref.Set(r.Context(), data)
   if err != nil {
      log.Err(err)
      http.Error(w, err.Error(), http.StatusInternalServerError)
      return
   }
   w.Write([]byte(ref.ID))
}
