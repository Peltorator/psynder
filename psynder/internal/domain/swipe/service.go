package swipe

import (
	"fmt"
	"github.com/peltorator/psynder/internal/domain"
	"github.com/peltorator/psynder/internal/pagination"
)

type Decision int

const (
	DecisionUndefined = iota
	DecisionLike
	DecisionSkip
)

var decisionToString = map[Decision]string {
	DecisionUndefined: "undefined",
	DecisionLike: "like",
	DecisionSkip: "skip",
}

var stringToDecision = make(map[string]Decision)

func init() {
	for decision, str := range decisionToString {
		stringToDecision[str] = decision
	}
}

func (d Decision) String() string {
	return decisionToString[d]
}

func DecisionFromString(s string) Decision {
	return stringToDecision[s]
}

type BrowseErrorKind int

const (
	BrowseErrorUnknown = iota
	BrowseErrorLimitTooBig
)

type BrowseError struct {
	Cause error
	Kind BrowseErrorKind
}

func (e BrowseError) Error() string {
	return fmt.Sprintf("browse error with kind=%v caused by: %v", e.Kind, e.Cause)
}

type AllInfo struct {
	Users int64 `json:"users"`
	Shelters int64 `json:"shelters"`
	Psynas int64 `json:"psynas"`
}

type Service interface {
	BrowsePsynas(uid domain.AccountId, pg pagination.Info, f domain.PsynaFilter) ([]Psyna, error)
	GetLikedPsynas(uid domain.AccountId, pg pagination.Info) ([]Psyna, error)
	RatePsyna(uid domain.AccountId, pid domain.PsynaId, decision Decision) error
	GetPsynaInfo(pid domain.PsynaId) (Shelter, error)
	GetAllInfo() (AllInfo, error)
}
