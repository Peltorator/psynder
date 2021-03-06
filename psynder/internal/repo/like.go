package repo

import (
	"github.com/peltorator/psynder/internal/domain"
	"github.com/peltorator/psynder/internal/domain/swipe"
	"github.com/peltorator/psynder/internal/pagination"
)

type Like struct {
	uid domain.AccountId
	pid domain.PsynaId
	decision swipe.Decision
}

type AllInfo struct {
	Users int64
	Shelters int64
	Psynas int64
}

type Likes interface {
	GetLikedPsynas(uid domain.AccountId, pg pagination.Info) ([]Psyna, error)
	RatePsyna(uid domain.AccountId, pid domain.PsynaId, decision swipe.Decision) error
	GetPsynaInfo(pid domain.PsynaId) (Shelter, error)
	GetAllInfo() (AllInfo, error)
}
