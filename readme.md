# Order book simulation

## Descriere:
Diverse entitati (Stock traders) plaseaza comezi de vanzare/cumparare de actiuni ale unor companii listate pe diferite Exchange-uri.
<br>
Exchange-urile proceseaza comenzile plasate si genereaza tranzactii. Exchange-urile permit anularea comenzilor active.
<br>
Comenzile sunt de 3 tipuri (LIMIT, ICEBERG si MARKET), fiecare avand un comportament diferit. Comenzile de tip LIMIT si MARKET sunt vizibile in Exchange, iar cele ICEBERG nu. Comenzile de tip LIMIT si ICEBERG au un pret fix stabilit de Stock Trader, iar cele de tip MARKET folosesc pretul curent al actiunilor companiei vizate.

## Obiecte:
Company - name, ticker, list of exchanges on which it is listed
<br>
StockTrader - name, list of active orders
<br>
<br>
Order - abstract class: id, type (buy/sell), trader, ticker, quantity, price, exchange
<br>
MarketOrder - visible on the exchange, price mirrors the current price of the stock
<br>
LimitOrder - visible on the exchange, fixed price
<br>
IcebergOrder - not visible on the exchange, fixed price
<br>
<br>
Transaction - timestamp, from, to, price, quantity
<br>
Exchange - name, list of bid orders, list of ask orders, list of all transactions
<br>
<br>
StockMarketSimulation - simulates a random stock market
<br>
StockMarketService - exposes functionality to Main via an interactive menu

## Interogari:
Add exchange
<br>
Add company
<br>
Add stock trader
<br>
List companies on exchange
<br>
Place order
<br>
Cancel order
<br>
Show transactions for company on exchange
<br>
Show active orders for company on exchange
<br>
Show active orders for stock trader
<br>
Show market price evolution for company

## Spin up DB:
```
docker run \
-e POSTGRES_USER=admin \
-e POSTGRES_PASSWORD=admin \
-e PGDATA=/var/lib/postgresql/data/pgdata \
-e POSTGRES_DB=pao \
-v ${PWD}/pgdata:/var/lib/postgresql/data \
-p 5555:5432 \
postgres:15.3-bullseye
```

## DB Schema:
EXCHANGES: exchange_name

COMPANIES: company_name, company_ticker

STOCK_TRADERS: stocktrader_name

ORDERS (outstanding): id, order_action(buy/sell), order_type(limit/iceberg/market), stocktrader_name, company_ticker, exchange_name, date, price, quantity

LISTED_ON: exchange_name, company_ticker

TRANSACTIONS: exchange_name, company_ticker, date, stocktrader_name_from, stocktrader_name_to, price, quantity
