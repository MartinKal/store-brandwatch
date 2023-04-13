# store-brandwatch
Store component

This repository contains the implementation of the Store component from the brandwatch assessment task. You also need to clone the Shop repository located at
https://github.com/MartinKal/shop-brandwatch <br />

## Setup
Requires docker

1. Clone this repository.
2. Navigate to Store app project root:

- execute ```docker network create shared-network``` to create docker network.
- create a JAR using ```./mvnw clean package``` if you are using IntelliJ
- execute docker-compose up. It should start 3 docker containers (Stock app, MongoDB, Redis).
Keep in mind the shop component requires the same Redis instance so it should be started after this one.
3. Navigate to Shop component root, create a JAR with ```./mvnw clean package```, and execute again ```docker-compose up``` to start the Shop app and it's MongoDb instance.
4. The 2 apps should be running at this point

## Store component api

Runs on ```http://localhost:8080```

### 1. Load Stock <br />
**URL:** /stock <br />
**Method:** POST <br />
**Request Body:** A JSON object containing a list of items with product ID and quantity. <br />
**Description:** This endpoint loads or updates the stock with the provided items. It also sends a message with the updated stock information
to a Redis topic triggering all pending orders that can be completed to be retried.<br/>

Request example:
```
{
  "items": [
    {
      "productId": "product1",
      "quantity": 5
    },
    {
      "productId": "product2",
      "quantity": 10
    }
  ]
}
```

Response example:<br />
```
{
  "items": [
    {
      "productId": "product1",
      "quantity": 5
    },
    {
      "productId": "product2",
      "quantity": 10
    }
  ]
}
```

### 2. Get All Stock Shortages <br />
**URL:** /stock/shortages <br />
**Method:** GET <br />
**Description:** This endpoint retrieves all stock shortages from the database. Stock shortages are products with a needed quantity greater than zero.<br/>
Response example:
```
[
  {
    "product1": 5
  },
  {
    "product2": 10
  }
]
```

### 3. URL: /products/process - for internal use

**Method:** POST
**Request Body:** A JSON object containing a list of items with product ID, quantity, and order reference ID. <br />
**Description:** This endpoint processes a shop order and checks if the required stock is available. 
It returns a result indicating whether the order can be completed or not.

Request body example:
```
{
  "orderReferenceId": "order123", // orderRefernceId can be omitted
  "items": [
    {
      "productId": "product1",
      "quantity": 5
    },
    {
      "productId": "product2",
      "quantity": 10
    }
  ]
}
```
Response example:
```
{
  "success": true,
  "orderReferenceId": "order123"
}
```

## Shop component api

Runs on ```http://localhost:8081```

**Create Order**<br/>
**Method: POST**<br/>
**Path:** /orders <br/>
**Description:** Creates a new order using the provided request data.<br/>

Request body example:
```
{
  "items": [
    {
      "productId": "<product_id>",
      "quantity": <quantity>
    },
    ...
  ]
}
```
items: An array of items in the order.<br/>
productId: The identifier of the product.<br/>
quantity: The quantity of the product in the order.<br/>

**Response**<br/>
**Status Code: 200 OK**<br/>
**Body:**<br/>
```
{
  "message": "<order_status>"
}
```
**message:** A string describing the order status. Possible values are:<br/>
- "Order completed.": The order was successfully completed.
- "Order pending.": The order is pending due to stock unavailability.

Usage
To create a new order, send a POST request to ```/orders``` with the required data in the request body.

Example:
```
curl -X POST -H "Content-Type: application/json" -d '{
  "items": [
    {
      "productId": "product-1",
      "quantity": 2
    },
    {
      "productId": "product-2",
      "quantity": 1
    }
  ]
}' http://localhost:8080/orders
```

A successful response will have a status code of 200 OK and a JSON object with the order status message:
```
{
  "message": "Order completed."
}
```
In case the order is pending due to stock unavailability, the response will be:
```
{
  "message": "Order pending."
}
```
