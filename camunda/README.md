## Camunda Variant

### Versions
- SpringBoot 2.4
- Camunda 7.15
- H2 (in Memory DB) 1.4
- JDK 11
- ZXing (Zebra Crossing) 3.4 for QRCode generation
- Gradle 7.1

## Process Model
<img src="src/main/resources/bpmn/ticket-reservation.png" width="900">

## How to run it
- start the application 
    - run `./gradlew bootRun` from your terminal
    - use your IDE to start the application
- go to `localhost:8087` to get to the camunda webapps
- use a REST client to make a reservation:
    - endpoint `localhost:8087/reservation`
    - POST request 
        - body: `{ "userId":"max.mustermann", "seats": [ "A1", "A2" ] }`
        - content-type: `application/json`
        - no auth necessary
- check the log output
