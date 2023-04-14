# store-brandwatch
Store component

This repository contains the implementation of the Store component from the brandwatch assessment task. You also need to clone the Shop repository located at
https://github.com/MartinKal/shop-brandwatch <br />

## Setup
Requires docker

1. Clone this repository.
2. Navigate to Store app project root:

- execute ```docker network create shared-network``` to create docker network.
- execute ```docker-compose up --build```. It should start 3 docker containers - store, mongo-store and redis-store. The Store is now running and listening to ```http://localhost:8090```<br/>

- now clone shop repository
- navigate to root folder
- execute ```docker-compose up --build```. It should start 2 docker containers - shop and mongo-shop. Shop is listening to ```http://localhost:8091```
Keep in mind the shop component requires the same Redis instance from the store, so it should be started after it.<br/>
3. The 2 apps should be running at this point

**Note** The two services (Shop, Store) can also be run from the IDE. But they are started on different ports: When started from the IDE, the Store is listening to localhost:8080 and the Shop - localhost:8081

## Store component
The Store component works with the products in stock. The user of this component is a supplier
who should be able to check if the Store needs to be loaded with more products required by clients
in the Shop component and load the needed products.<br/>

## Store component api

Runs on ```http://localhost:8090```

### 1. Load Stock <br />
**URL:** /stock <br />
**Method:** POST <br />
**Request Body:** A JSON object containing a list of items with product ID and quantity. <br />
**Description:** This endpoint loads or updates the stock with the provided items. It returns the updated stock information before pending orders are completed. So if there are any pending orders that can be completed, this current in stock information will be outdated.<br/>
**Load Stock endpoint** also sends a message to a Redis streams topic. When the shop reads this message sent from the store, it triggers the retry of all pending orders that have not been completed due to a shortage of the required items.<br/>

Request example:
```
{
  "items": [
    {
      "productId": "<product_id>",
      "quantity": <quantity>
    },
    {
      "productId": "<product_id>",
      "quantity": <quantity>
    }
  ]
}
```

Response example:<br />
```
{
  "items": [
    {
      "productId": "<product_id>",
      "quantity": <quantity>
    },
    {
      "productId": "<product_id>",
      "quantity": <quantity>
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
    "product1": <quantity>
  },
  {
    "product2": <quantity>
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
  "orderReferenceId": "<orderId>", // orderRefernceId can be omitted
  "items": [
    {
      "productId": "<product_id>",
      "quantity": <quantity>
    },
    {
      "productId": "<product_id>",
      "quantity": <quantity>
    }
  ]
}
```
Response example:
```
{
  "success": true,
  "orderReferenceId": "<order_id>"
}
```
## Shop component
The Shop component allows the clients to order more or new products. When the products are
available, their orders are processed, and the products are taken from the store.<br/>

## Shop component api

Runs on ```http://localhost:8091```

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
      "productId": "<product_id>",
      "quantity": <quantity>
    },
    {
      "productId": "<product_id>",
      "quantity": <quantity>
    }
  ]
}' http://localhost:8091/orders
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
