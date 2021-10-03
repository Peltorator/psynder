package usecases

import (
	"psynder/internal/domain/model"
	"psynder/internal/domain/repo"
)



type SwipeUseCases interface {
	LoadPsynas(opts repo.LoadPsynasOptions) ([]model.Psyna, error)
	LikePsyna(opts repo.LikePsynaOptions) error
	GetFavoritePsynas(id model.AccountId) ([]model.Psyna, error)
}

type SwipeUseCasesImpl struct {
	SwipeRepo repo.SwipeRepo
}

func NewSwipeUseCases(swipeRepo repo.SwipeRepo) *SwipeUseCasesImpl {
	return &SwipeUseCasesImpl{
		SwipeRepo: swipeRepo,
	}
}

func (u *SwipeUseCasesImpl) LoadPsynas(opts repo.LoadPsynasOptions) ([]model.Psyna, error) {
	psynas, err := u.SwipeRepo.LoadPsynas(opts)
	if err != nil {
		return []model.Psyna{}, err
	}
	var psynasId []model.PsynaId
	maxId := model.PsynaId(0)
	for i := 0; i < len(psynas); i++ {
		psynasId = append(psynasId, psynas[i].Id)
		if psynas[i].Id > maxId {
			maxId = psynas[i].Id
		}
	}
	err = u.SwipeRepo.SaveLastView(opts.AccountId, maxId)
	if err != nil {
		return []model.Psyna{}, err
	}
	return psynas, nil
}

func (u *SwipeUseCasesImpl) LikePsyna(opts repo.LikePsynaOptions) error {
	err := u.SwipeRepo.LikePsyna(opts)
	return err
}

func (u *SwipeUseCasesImpl) GetFavoritePsynas(id model.AccountId) ([]model.Psyna, error) {
	psynas, err := u.SwipeRepo.GetFavoritePsynas(id)
	return psynas, err
}