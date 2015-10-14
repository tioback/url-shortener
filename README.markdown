## Chaordic Coding Challenge - URLShortener - Renato Back

This projects provides web service that allows users to either shorten a URL or retrieve the stretched version of a shortened one.

Follow these steps to get started:

1. Git-clone this repository.

        $ git clone git://github.com/tioback/url-shortener.git url-shortener

2. Change directory into your clone:

        $ cd url-shortener

3. Launch SBT:

        $ sbt

4. Compile everything and run all tests:

        > test

5. Start the application:

        > re-start

6. Use a REST Client to use the system through [http://localhost:8080](http://localhost:8080/)

7. To shorten, do a POST request to [http://localhost:8080/encurte/url](http://localhost:8080/encurte/url) and with a json:

	{
		"longUrl": "http://www.chaordic.com”
	}

8. To stretch, do a GET request to [http://localhost:8080/url?shortUrl=http://chrdc.co/a](http://localhost:8080/url?shortUrl=http://chrdc.co/a)

9. Stop the application:

        > re-stop
