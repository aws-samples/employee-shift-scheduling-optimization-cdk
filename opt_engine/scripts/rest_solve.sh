#!/bin/sh

HTTP_ENDPOINT="http://127.0.0.1:8080/schedule/solve"

#DATA="@rest_solve.json"
#DATA="@rest_solve_s3e2r1.json"
#DATA="@rest_solve_s5e5r4.json"
#DATA="@rest_solve_s5e5r4_nf.json"
DATA="@rest_solve_s14e5r4.json"

curl -X POST $HTTP_ENDPOINT -H "Content-Type: application/json" -d $DATA
