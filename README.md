# yahoo-finance-Scraper
A scraper that collects the data from yahoo finance and uploads it to a mongodb collection

This scraper will scrape these tables that are available on Yahoo finance:
Cryptocurrencies
Stocks: Most Actives
Stocks: Gainers
Stocks: Losers
Futures
World Indices
Currencies
Top Mutual Funds

aster scraping the data from the website it will store all of them in their own database collection in mongodb.
There is an other collection that will keep track for collections 
that are starting a new cycle of update, there will change there collection "card" status to updating, this can be used to avoid a thread 
from taking old or even empty collection from the database.

Collection samples of the collection status table:
![Capture](https://user-images.githubusercontent.com/66326085/120664779-e4098d80-c493-11eb-904b-fecb525e0bba.JPG)

Collection samples of the tables collections :
![Capture](https://user-images.githubusercontent.com/66326085/120664706-d5bb7180-c493-11eb-9b76-7d22001c3a8a.JPG)
![Capture2](https://user-images.githubusercontent.com/66326085/120664711-d6540800-c493-11eb-87be-9f11a7da0862.JPG)
![Capture3](https://user-images.githubusercontent.com/66326085/120664568-b4f31c00-c493-11eb-8552-ddce1e13997a.JPG)
![Capture4](https://user-images.githubusercontent.com/66326085/120664712-d6ec9e80-c493-11eb-8b2b-be5b1fc96453.JPG)


feel free to E-mail me if you have something to ask, i will be more then happy to help :-).

best regards
Ben-David Benjamin.


