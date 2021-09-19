package httpapi

import (
	"fmt"
	"github.com/gorilla/mux"
	"net/http"
	"psynder/internal/usecases"
)

type Api struct {
	AccountUseCases usecases.AccountUseCases
	JSONHandler JSONHandler
}

func New(a usecases.AccountUseCases) *Api {
	return &Api{
		AccountUseCases: a,
	}
}

func (a *Api) Router() http.Handler {
	r := mux.NewRouter()

	r.HandleFunc("/signup", handleErrorResponses(a.postSignup)).Methods(http.MethodPost)
	r.HandleFunc("/login", handleErrorResponses(a.postLogin)).Methods(http.MethodPost)

	return r
}

// postSignup handles request for a new account creation.
func (a *Api) postSignup(w http.ResponseWriter, r *http.Request) error {
	var m postSignupRequest
	if err := a.JSONHandler.ReadJson(r, &m); err != nil {
		return err
	}

	accId, err := a.AccountUseCases.CreateAccount(usecases.CreateAccountOptions{
		Email:    m.Email,
		Password: m.Password,
	})
	if err != nil {
		return err
	}

	location := fmt.Sprintf("/accounts/%s", accId.String())
	w.Header().Set("Location", location)
	w.WriteHeader(http.StatusCreated)
	return nil
}

// postLogin handles login request for existing user.
func (a *Api) postLogin(w http.ResponseWriter, r *http.Request) error {
	var m postSignupRequest
	if err := a.JSONHandler.ReadJson(r, &m); err != nil {
		return err
	}

	token, err := a.AccountUseCases.LoginToAccount(usecases.LoginToAccountOptions{
		Email:    m.Email,
		Password: m.Password,
	})
	if err != nil {
		return err
	}

	w.Header().Set("Content-Type", "application/jwt")
	if _, err := w.Write([]byte(token)); err != nil {
		return err
	}
	return nil
}
