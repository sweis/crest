Crest: Crypto + REST
=============

This is just playing with hooking up a Crypto REST API.

Generate a 2048-bit RSA private key (without a passphrase):

	openssl genrsa -out privkey.pem 2048

Export the RSA public key in PEM format:

	openssl rsa -in privkey.pem -pubout -out pubkey.pem

Alternatively, export the RSA public key in DER format:

	openssl rsa -in privkey.pem -pubout -outform DER -out pubkey.der

PUT a PEM-encoded RSA public key on a server and output its SHA-1 hash:

	curl -T pubkey.pem http://localhost:8080/crest/v1/publickey > pub-key-hash.txt

Get a SHA-1 hash of a DER-encoded public key:

	openssl sha1 < pubkey.der

GET a PEM-encoded public key by the SHA-1 hash of its DER-encoded value:

	curl http://localhost:8080/crest/v1/publickey/{pubKeyHash}

Examples:

	curl http://localhost:8080/crest/v1/publickey/`openssl sha1 < pubkey.der`
	curl http://localhost:8080/crest/v1/publickey/`cat pub-key-hash.txt`

TODO: PUT data to encrypt with a wrapped session key:

	curl -T filename http://localhost:8080/crest/v1/encrypt/{pubKeyHash} > session-key-hash.txt


