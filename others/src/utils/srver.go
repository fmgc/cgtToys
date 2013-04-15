package main

import (
	"fmt"
	"net/http"
	"io"
	"io/ioutil"
	"log"
)

func admin(w http.ResponseWriter, req *http.Request) {
	resstring := fmt.Sprintf("<p>The id is %s</p>", "moocow")
	io.WriteString(w, resstring)
}

func banana(w http.ResponseWriter, req *http.Request) {
	contents, _ := ioutil.ReadFile("madgo.html")
	io.WriteString(w, string(contents))
}

func main() {
	http.HandleFunc("/admin", admin)
	http.HandleFunc("/banana", banana)
	err := http.ListenAndServe(":8080", nil)
	if err != nil {
		log.Fatal(err)
	}
}
