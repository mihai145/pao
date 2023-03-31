# Order book simulation

Company - name, ticker, list of exchanges on which it is listed
<br>
StockTrader - name, list of active orders (references)
<br>
<br>
Order - abstract class; type (buy/sell), trader (reference), ticker, quantity, exchange on which it is placed (reference)
<br>
? MarketOrder - buy/sell at current market price (if it exists)
<br>
LimitOrder - bid/ask price; visible on the exchange
<br>
IcebergOrder - bid/ask price; not visible on the exchange
<br>
<br>
Transaction - timestamp, from, to, price, quantity
<br>
Exchange - name, list of bid orders, list of ask orders, list of all transactions (by stock ticker) 
<br>
<br>
StockMarketService - exposes functionality to main