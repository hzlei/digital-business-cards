package handlers

import (
	"net/http"

	"github.com/google/uuid"
	"github.com/rs/zerolog/log"
)

func CreateUserIdHandler(w http.ResponseWriter, r *http.Request) {
   user_id, err := uuid.NewUUID()
   if err != nil {
      log.Err(err)
      http.Error(w, err.Error(), http.StatusInternalServerError)
      return
   }
   // doc, err := Client.Collection("users").Doc(user_id.String()).Get(r.Context())
   // _ = doc
   // if err == nil {
   //    msg := "Failed to created unique id. Please try again!"
   //    log.Error().Msg(msg)
   //    http.Error(w, msg, http.StatusInternalServerError)
   //    return
   // }
   //
   // var data = make(map[string]interface{})
   // new_doc, err := Client.Collection("users").Doc(user_id.String()).Set(r.Context(), data)
   // _ = new_doc
   // if err != nil {
   //    log.Err(err)
   //    http.Error(w, err.Error(), http.StatusInternalServerError)
   //    return
   // }
   w.Write([]byte(user_id.String()))
}
