package main

import (
	"fmt"
	"io"
	"log"
	"net/http"
)

func handler(w http.ResponseWriter, req *http.Request) {
	fmt.Println(*req)
	io.WriteString(w, "hello, world!\n")
}

func main() {
	http.HandleFunc("/hello", handler)
	log.Printf("About to listen on 8080. Go to https://127.0.0.1:10443/")
	err := http.ListenAndServe(":8080", nil)
	if err != nil {
		log.Fatal(err)
	}
}
