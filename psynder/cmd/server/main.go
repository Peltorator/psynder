package main

import (
	"flag"
	"fmt"
	"github.com/ilyakaznacheev/cleanenv"
	"io/ioutil"
	"net/http"
	"psynder/internal/domain/repo"
	"psynder/internal/interface/httpapi"
	"psynder/internal/service/token"
	"psynder/internal/usecases"
	"time"
)

func main() {
	const readTimeout = 10 * time.Second
	const writeTimeout = 10 * time.Second

	yamlConfigPath := flag.String("config", "config.yml", "path to the YAML config file")
	flag.Parse()

	var cfg AppConfig
	err := cleanenv.ReadConfig(*yamlConfigPath, &cfg)
	if err != nil {
		panic(err)
	}

	privateBytes, err := ioutil.ReadFile(cfg.JWT.KeyPath)
	if err != nil {
		panic(err)
	}
	publicBytes, err := ioutil.ReadFile(cfg.JWT.PublicKeyPath)
	if err != nil {
		panic(err)
	}
	tokenIssuer, err := token.NewJwtHandler(privateBytes, publicBytes, cfg.JWT.TokenDuration)
	if err != nil {
		panic(err)
	}

	a := httpapi.New(
		usecases.NewAccountUseCases(repo.NewInMemoryAccountRepo(), tokenIssuer),
		httpapi.NewJSONHandler())

	addr := fmt.Sprintf("%v:%v", cfg.Server.Host, cfg.Server.Port)
	fmt.Printf("Starting on %v...\n", addr)
	server := http.Server{
		Addr:         addr,
		ReadTimeout:  readTimeout,
		WriteTimeout: writeTimeout,
		Handler:      a.Router(),
	}

	//err = server.ListenAndServe()
	err = server.ListenAndServeTLS(cfg.TLS.CertPath, cfg.TLS.KeyPath)
	if err != nil {
		panic(err)
	}
}
