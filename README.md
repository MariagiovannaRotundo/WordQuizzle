<h1 align="center">
  Laboratorio di Reti, Corsi A e B </br>
  Word Quizzle (WQ) </br>
  Progetto di Fine Corso </br>
  A.A. 2019/20
</h1></br></br>


<h2>1. Descrizione del Problema</h2>
<p align="justify">
  Il progetto consiste nell’implementazione di un sistema di sfide di traduzione  italiano-inglese tra utenti registrati al servizio.
  Gli utenti registrati possono sfidare i propri   amici ad una gara il cui
  scopo è quello di tradurre in inglese il maggiore numero di parole italiane   proposte dal servizio.
  Il sistema consente inoltre la gestione di una rete sociale tra gli utenti  iscritti.
  L’applicazione è implementata secondo una architettura client server.
</p>

<h2>2. WQ: specifica delle operazioni</h2>
<p align="justify">
  Di seguito sono specificate le operazioni offerte dal servizio WQ.
  In sede di implementazione è possibile aggiungere ulteriori parametri se necessario.
</p>

  <ul>
    <li>
      <p align="justify">
        <b><i>registra_utente(nickUtente,password)</i>:</b> per inserire un nuovo utente.<br> 
        Il server risponde con un codice che può indicare l’avvenuta registrazione, oppure, se il nickname è già presente, 
        o se la password è vuota, restituisce un messaggio d’errore.<br> 
        Come specificato in seguito, le registrazioni sono tra le informazioni da persistere.
      </p>
    </li>
    <li>
      <p align="justify">
        <b><i>login(nickUtente, password)</i>:</b> login di un utente già registrato per accedere al  servizio.<br>
        Il server risponde con un codice che può indicare l’avvenuto login, oppure, se l’utente ha  già effettuato il login o la password è errata, restituisce un messaggio d’errore.
      </p>
    </li>
    <li>
      <p align="justify">
        <b><i>logout(nickUtente)</i>:</b> effettua il logout dell’utente dal servizio.
      </p>
    </li>
    <li>
      <p align="justify">
        <b><i>aggiungi_amico (nickUtente, nickAmico)</i>:</b> aggiungere  un amico alla cerchia di amici di un utente.<br>
        Viene creato un arco non orientato tra i due utenti  (se A è amico di B, B è amico di A).<br>
        Il Server risponde con un codice che indica l’avvenuta   registrazione dell’amicizia oppure con un codice di errore, 
        se il nickname del nodo destinazione/sorgente della richiesta non esiste, oppure se è stato richiesto di creare una relazione di  amicizia già esistente.<br> 
        Non è necessario che il server richieda l’accettazione dell’amicizia da  parte di nickAmico.
      </p>
    </li>
     <li>
      <p align="justify">
        <b><i>lista_amici(nickUtente)</i>:</b> utilizzata da un utente per visualizzare la lista dei  propri amici, 
        fornendo le proprie generalità.<br>
        Il server restituisce un oggetto JSON che rappresenta la   lista degli amici.
      </p>
    </li>
    <li>
      <p align="justify">
        <b><i>sfida(nickUtente, nickAmico)</i>:</b> l’utente nickUtente intende sfidare l’utente di nome  nickAmico.<br>
        Il server controlla che nickAmico appartenga alla lista di amicizie di nickUtente, in  caso negativo restituisce un codice di errore e l’operazione termina.<br>
        In caso positivo, il  server invia a nickAmico una richiesta di accettazione della sfida e, 
        solo dopo che la richiesta è stata accettata, la sfida può avere inizio 
        (se la risposta non è stata ricevuta entro un  intervallo di tempo T1 si considera la sfida come non accettata).<br> 
        La sfida riguarda la  traduzione di una lista di parole italiane in parole inglesi, nel minimo tempo possibile.<br>
        Il server sceglie, in modo casuale, K parole da un dizionario contenente N parole italiane da inviare successivamente, una alla volta, ai due sfidanti.<br>
        La partita può durare al  massimo un intervallo di tempo T2. Il server invia ai partecipanti la prima parola.<br>
        Quando il  giocatore invia la traduzione (giusta o sbagliata), il server invia la parola successiva a quel  giocatore.<br>
        Il gioco termina quando entrambi i giocatori hanno inviato le traduzioni alle K parole o quando scade il timer.<br>
        La correttezza della traduzione viene controllata dal server utilizzando un servizio esterno, come specificato nella sezione seguente.<br>
        Ogni traduzione corretta assegna X punti al giocatore;
        ogni traduzione sbagliata assegna Y punti negativi;
        il giocatore con più punti  vince la sfida ed ottiene Z punti extra.<br> Per ogni risposta non inviata (a causa della scadenza del  timer) si assegnano 0 punti.<br>
        Il punteggio ottenuto da ciascun partecipante alla fine della  partita viene chiamato punteggio partita.<br>
        I valori espressi come K, N, T1, T2, X, Y e Z sono a discrezione dello studente.<br><br>
        <i>Esempio di svolgimento della partita</i>: I giocatori U1 e U2 si sfidano.<br>
        A inizio partita il server seleziona le parole  “<i>Bottiglia</i>”, “<i>Quarantadue</i>” e “<i>Rete</i>” dal dizionario.<br>
        Interroga il servizio e memorizza le   traduzioni. Infine il server setta un timeout della partita di 1 minuto.<br>
        Il server invia “<i>Bottiglia</i>” ad entrambi i giocatori.<br>
        U1 risponde con “<i>Bottle</i>”: il   server assegna 2 punti a U1 (punteggio corrente di U1: 2) e invia “<i>Quarantadue</i>”.<br>
        Nel frattempo U2  risponde con “<i>Botle</i>”, sottrae 1 punto a U2 (punteggio corrente di U2: -1) e invia “<i>Quarantadue</i>” a U2.<br>
        Supponendo che U1 sbagli le 2 parole successive, alla fine della partita totalizza  un punteggio di 0 punti (+2 -1 -1),
        e supponendo che U2 indovini entrambe le parole successive,  totalizza un punteggio di 3 punti (-1 +2 +2).<br>
        Il server dichiara U2 vincitore ed assegna ad U2 3 punti extra.
      </p>
    </li>
    <li>
      <p align="justify">
        <b><i>mostra_punteggio(nickUtente)</i>:</b> il server restituisce il punteggio di nickUtente (<i>chiamato “punteggio utente”</i>)
        totalizzato in base ai punteggi partita ottenuti in tutte le  sfide che ha effettuato.
      </p>
    </li>
    <li>
      <p align="justify">
        <b><i>mostra_classifica(nickUtente)</i>:</b> Il server restituisce in formato JSON la classifica calcolata in base ai punteggi utente ottenuti da nickUtente e dai suoi amici.
      </p>
    </li>
  </ul>

  <h2>3. WQ: specifiche per l'implementazione</h2>
    <p align="justify">
      Nella realizzazione del progetto devono essere utilizzate molte tecnologie illustrate durante il corso.<br>
      In particolare:
    </p>
      <ul>
        <li>
          <p align="justify">
            la fase di registrazione viene implementata mediante RMI.
          </p>
        </li>
        <li>
          <p align="justify">
            La fase di login deve essere effettuata come prima operazione dopo aver instaurato una connessione TCP con il server.<br>
            Su questa connessione TCP, dopo previa login effettuata con successo, avvengono le interazioni client-server (richieste/risposte).
          </p>
        </li>
        <li>
          <p align="justify">
            Il server inoltra la richiesta di sfida originata da nickUtente all'utente nickAmico usando la comunicazione UDP.
          </p>
        </li>
        <li>
          <p align="justify">
            Il server può essere realizzato multithreaded oppure può effettuare il multiplexing dei canali mediante NIO.
          </p>
        </li>
        <li>
          <p align="justify">
            Il server gestisce un dizionario di N parole italiane, memorizzato in un file.<br>
            Durante la fase di setup di una sfida fra due utenti il server seleziona K parole a caso su N parole presenti nel dizionario.<br>
            Prima dell’inizio della partita, ma dopo che ha ricevuto l’accettazione della sfida da parte dell’amico, il server chiede, tramite una chiamata HTTP GET,
            la traduzione delle parole selezionate al servizio esterno accessibile alla URL https://mymemory.translated.net/doc/spec.php.<br>
            Le traduzioni vengono memorizzate per tutta la durata della partita per verificare la correttezza delle risposte inviate dal client.
          </p>
        </li>
        <li>
          <p align="justify">
            L'utente interagisce con WQ mediante un client che può utilizzare una semplice interfaccia grafica, oppure una interfaccia a linea di comando, definendo un insieme di comandi, presentati in un menu.
          </p>
        </li>
        <li>
          <p align="justify">
            Il server persiste le informazioni di registrazione, relazioni di amicizia e punteggio degli utenti su file json.
          </p>
        </li>
      </ul>
    






 

 