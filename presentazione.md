# Ordine in cui effettuare la dimostrazione

## Comportamenti ammessi

1. Login scorretto e poi corretto
1. Faccio notare che  che non c'è la scritta "copia qui"/"dropHere"/voce nel form se una categoria ha già più di 9 figli
1. Faccio notare che c'è la scritta "nessun padre", sia quando faccio copia/drag&drop che nel form a destra
1. Aggiungo qualcosa nella root facendo la copia dall'albero (copua/drag&drop)
1. Faccio notare che è scomparsa la scritta "nessun padre" e che è comparsa la voce nel form
1. Inserisco una classe dal form a destra
1. Nela versione RIA, rinomino una categoria con il click su di essa
1. Faccio vedere che è possibile aprire le immagini
1. Nella versione pureHtml uploado una immagine

## Comportamenti non ammessi 

1. Login con campi vuoti
1. Accedere alla pagina senza avere fatto il login
1. Copia ricorsiva nell'albero /CopyHere?id_from=405&id_to=423
1. Copia di una categoria in se stessa /CopyHere?id_from=405&id_to=405
1. Aggiungere figli ad una categoria che ha già più di 9 figli /CopyHere?id_from=405&id_to=411, /AppendCategory?father=411&name=nome
1. Parametri scorretti /CopyHere?id_from=ciaoo&id_to=tooo
1. Campo di rinomina vuoto (RIA)