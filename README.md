# Toy2_Compiler

**Toy2_Compiler** è un compilatore front-end per il linguaggio Toy2, sviluppato come parte del corso di Compilatori presso l'Università di Salerno. Il progetto utilizza **Java**, **JFlex**, e **JCup** per l'analisi lessicale e sintattica.

## Specifiche del Linguaggio Toy2

Le specifiche del linguaggio Toy2 sono disponibili nel documento [Specifiche_Toy2.pdf](https://github.com/matthew-2000/Toy2_Compiler/blob/main/specificheToy2/descrizione%20Toy2_2gen.pdf).  
Contengono una descrizione dettagliata della grammatica, sintassi e funzionalità supportate dal linguaggio.

## Struttura del Progetto

- **src**: codice sorgente del compilatore.
- **srcjflexcup**: file di configurazione per JFlex e JCup.
- **tests**: test per verificare il funzionamento del compilatore.

## Requisiti

Prima di procedere, assicurati di avere installati:

- **Java Development Kit (JDK)**: versione 8 o successiva.
- **Apache Maven**: per la gestione delle dipendenze e la costruzione del progetto.

## Costruzione del Progetto

Per costruire il progetto, esegui il seguente comando nella directory principale:

```bash
mvn clean install
```

Questo comando compilerà il progetto e genererà un file JAR eseguibile nella directory `target`.

## Esecuzione del Compilatore

Per utilizzare il compilatore su un file sorgente Toy2, esegui:

```bash
java -jar target/Toy2_Compiler.jar path/to/source.toy2
```

Sostituisci `path/to/source.toy2` con il percorso del file sorgente Toy2 da compilare.

## Contribuire al Progetto

I contributi sono benvenuti!  
Per proporre modifiche o miglioramenti:

1. Forka il repository.
2. Crea un branch per le tue modifiche.
3. Invia una pull request al repository originale.

In alternativa, puoi segnalare problemi o bug utilizzando le issue del repository.

## Licenza

Questo progetto è distribuito sotto la licenza **MIT**.  
Consulta il file `LICENSE` per ulteriori dettagli.
