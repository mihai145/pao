# model.order.Order book simulation

## Descriere:
Diverse entitati (Stock traders) plaseaza comezi de vanzare/cumparare de actiuni ale unor companii listate pe diferite model.exchange.Exchange-uri.
<br>
model.exchange.Exchange-urile proceseaza comenzile plasate si genereaza tranzactii. model.exchange.Exchange-urile permit anularea comenzilor active.
<br>
Comenzile sunt de 3 tipuri (LIMIT, ICEBERG si MARKET), fiecare avand un comportament diferit. Comenzile de tip LIMIT si MARKET sunt vizibile in model.exchange.Exchange, iar cele ICEBERG nu. Comenzile de tip LIMIT si ICEBERG au un pret fix stabilit de Stock Trader, iar cele de tip MARKET folosesc pretul curent al actiunilor companiei vizate.

## Obiecte:
model.company.Company - name, ticker, list of exchanges on which it is listed
<br>
model.stocktrader.StockTrader - name, list of active orders
<br>
<br>
*model.order.Order - abstract class: id, type (buy/sell), trader, ticker, quantity, price, exchange*
<br>
model.order.MarketOrder - visible on the exchange, price mirrors the current price of the stock
<br>
model.order.LimitOrder - visible on the exchange, fixed price
<br>
model.order.Order.IcebergOrder - not visible on the exchange, fixed price
<br>
<br>
model.exchange.Transaction - timestamp, from, to, price, quantity
<br>
model.exchange.Exchange - name, list of bid orders, list of ask orders, list of all transactions
<br>
<br>
StockMarketSimulation - simulates a random stock market
<br>
service.StockMarketService - exposes functionality to Main via an interactive menu

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