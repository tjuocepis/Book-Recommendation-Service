** Book Recommendation System **

*** What is the project about? ***

The project is a book recommendation system that asks the user to rate books and based on that it calculates
recommendations and displays them to the user.

*** Design Rationale ***

The project includes a local client that the user can use to interact with the system.  The client program will ask
the user to input some book recommendations and then will generate the recommendations and display them to output.
The system consists of two remote databases (MySQL and NoSQL) that sit on Google Cloud.  One database is Google MySQL
and the other one is Elastic Search which sits on a GCE Virtual Machine.  While technically Elastic Search is not a
NoSQL database, in this project I use it as a cache for storing recommendations so that they can be retrieved if the
user already had their recommendations calculated since it takes some time (about 30 to 60 seconds) to generate new
recommendations.  When the user inputs their name in the beginning, the system will check Elastic Search to see if
there are recommendations for that user.  If there are none it will take them to the menu for recommending books.
If there are recommendations then it will display them and ask the user if they would like to get new recommendations.
I use Elastic Search also as a search engine to retrieve books that the client wants to rate.  MySQL database is used
to store book meta data as well the book's image url.  When generating the recommendations the algorithm does it
based on book ISBN numbers so to make a full recommendations response it queries MySQL based on book ISBN numbers to
grab the meta data such as the title, author and etc.

*** MySQL Table Schema ***

+----------------------------------------------------+
| isbn | title | author | year | publisher | image_l |
+----------------------------------------------------+
| ...  | ..... | ...... | .... | ......... | ....... |
+----------------------------------------------------+

*** Experiences ***

I ran into a nasty issue with a duplicate Guava dependency that took forever to resolve.  Because of that I was not
able to deploy my system to a VM because it would not compile. Once I fixed the issue I did not have enough time
to do so

*** Documentation ***

For documentation go to documentation folder and open index.html