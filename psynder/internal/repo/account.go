package repo

import (
	"fmt"
	"github.com/peltorator/psynder/internal/domain"
)

type AccountData struct {
	LoginCredentials
	Kind domain.AccountKind
}

type Account struct {
	Id domain.AccountId
	AccountData
}


type AccountStoreErrorKind int

const (
	AccountStoreErrorUnknown = iota
	AccountStoreErrorDuplicate
)

type AccountStoreError struct {
	Cause error
	Kind AccountStoreErrorKind
}

func (e AccountStoreError) Error() string {
	return fmt.Sprintf("account store error with kind=%v caused by: %v", e.Kind, e.Cause)
}

type AccountLoadErrorKind int

const (
	AccountLoadErrorUnknown = iota
	AccountLoadErrorNoSuchEmail
)

type AccountLoadError struct {
	Cause error
	Kind AccountLoadErrorKind
}

func (e AccountLoadError) Error() string {
	return fmt.Sprintf("account load error with kind=%v caused by: %v", e.Kind, e.Cause)
}

type Accounts interface {
	StoreNew(data AccountData) (domain.AccountId, error)
	LoadByEmail(email string) (Account, error)
}
