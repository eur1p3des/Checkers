package edu.upc.epsevg.prop.checkers.players;

import edu.upc.epsevg.prop.checkers.CellType;
import edu.upc.epsevg.prop.checkers.GameStatus;
import edu.upc.epsevg.prop.checkers.IAuto;
import edu.upc.epsevg.prop.checkers.IPlayer;
import edu.upc.epsevg.prop.checkers.MoveNode;
import edu.upc.epsevg.prop.checkers.PlayerMove;
import edu.upc.epsevg.prop.checkers.PlayerType;
import edu.upc.epsevg.prop.checkers.SearchType;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Estratègia de jugador automàtic que implementa l'algorisme MiniMax Iterative
 * Deepening Search per a les dames. El jugador busca la millor jugada possible
 * tenint en compte una heurística específica. Les opcions de moviment es
 * generen fins a una determinada profunditat de l'arbre de cerca. Es pot
 * especificar la profunditat en el constructor.
 *
 * @author Ernest Anguera Aixalà
 * @author Naïm Barba Morilla
 *
 * @version 1.0
 */
public class PlayerID implements IPlayer, IAuto {

    private String name = "Mickey La Rata Iterativa";
    private int nodesExplorats;
    private int profunditat;
    private boolean timeout;
    private int millorValorHeuristic;
    private int heuristicaActual;
    private PlayerType jugadorMaxim;
    private PlayerType jugadorMinim;

    public PlayerID() {
        nodesExplorats = 0;
        profunditat = 0;
        timeout = false;
        millorValorHeuristic = heuristicaActual = -20000;
    }

    @Override
    public void timeout() {
        timeout = true;
    }

    @Override
    public PlayerMove move(GameStatus gs) {
        nodesExplorats = 0;
        profunditat = 0;
        timeout = false;
        millorValorHeuristic = heuristicaActual = -20000;
        jugadorMaxim = gs.getCurrentPlayer();
        jugadorMinim = PlayerType.opposite(jugadorMaxim);
        List<Point> moviment = ids(gs);
        return new PlayerMove(moviment, nodesExplorats, profunditat, SearchType.MINIMAX);
    }

    private List<Point> ids(GameStatus gs) {
        int depth = 1;
        List<Point> millorMoviment = new ArrayList<>();

        while (!timeout) {
            List<Point> moviment = miniMax(gs, depth);
            if (heuristicaActual > millorValorHeuristic) {
                millorMoviment = moviment;
                millorValorHeuristic = heuristicaActual;
            }
            depth++;
        }
        profunditat = depth;
        return millorMoviment;
    }

    private List<Point> miniMax(GameStatus gs, int depth) {
        List<Point> moviment = new ArrayList<>();

        int valorHeuristic = -2000;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;
        List<List<Point>> camins = obtenirMoviments(gs.getMoves());
        for (List<Point> cami : camins) {
            if (timeout) {
                break;
            }
            GameStatus aux = new GameStatus(gs);
            aux.movePiece(cami);

            int heuristica = minValor(aux, depth - 1, alpha, beta);
            // Actualitzar el millor moviment si es troba un valor heurístic millor
            if (heuristica > valorHeuristic) {
                valorHeuristic = heuristica;
                moviment = cami;
            }

            // Actualitzar el valor d'alpha
            alpha = Math.max(alpha, heuristicaActual);
        }
        heuristicaActual = valorHeuristic;
        return moviment;
    }

    private int minValor(GameStatus gs, int depth, int alpha, int beta) {
        int valorHeuristic = 10000;
        if (gs.isGameOver()) {
            if (gs.GetWinner() == jugadorMaxim) {
                return valorHeuristic;
            }
            if (gs.GetWinner() != jugadorMaxim && gs.GetWinner() != jugadorMinim) {
                return 0;
            }
        }
        if (depth == 0) {
            return evaluarEstat(gs);
        }
        List<List<Point>> camins = obtenirMoviments(gs.getMoves());
        for (List<Point> cami : camins) {
            if (timeout) {
                break;
            }
            GameStatus aux = new GameStatus(gs);
            aux.movePiece(cami);

            int heuristica = maxValor(aux, depth - 1, alpha, beta);
            valorHeuristic = Math.min(valorHeuristic, heuristica);
            beta = Math.min(beta, valorHeuristic);
            if (alpha >= beta) {
                break;
            }
        }
        return valorHeuristic;
    }

    private int maxValor(GameStatus gs, int depth, int alpha, int beta) {
        int valorHeuristic = -10000;
        if (gs.isGameOver()) {
            if (gs.GetWinner() == jugadorMinim) {
                return valorHeuristic;
            }
            if (gs.GetWinner() != jugadorMaxim && gs.GetWinner() != jugadorMinim) {
                return 0;
            }
        }
        if (depth == 0) {
            return evaluarEstat(gs);
        }
        List<List<Point>> camins = obtenirMoviments(gs.getMoves());
        for (List<Point> cami : camins) {
            if (timeout) {
                break;
            }
            GameStatus aux = new GameStatus(gs);
            aux.movePiece(cami);
            int heuristica = minValor(aux, depth - 1, alpha, beta);
            valorHeuristic = Math.max(heuristica, valorHeuristic);
            alpha = Math.max(valorHeuristic, alpha);
            if (alpha >= beta) {
                break;
            }
        }
        return valorHeuristic;
    }

    private List<List<Point>> obtenirMoviments(List<MoveNode> moviments) {
        // Llista que emmagatzemarà els camins resultants
        List<List<Point>> resultats = new ArrayList<>();

        // Iterar a través dels moviments
        for (MoveNode moviment : moviments) {
            // Crear un nou camí que comença amb el punt del moviment actual
            List<Point> cami = new ArrayList<>();
            cami.add(moviment.getPoint());
            // Cridar a la funció auxiliar per obtenir els altres punts del camí
            obtenirMovimentsAuxiliars(moviment, cami, resultats);
        }

        // Retornar els camins resultants ordenats
        resultats.sort(Comparator.comparingInt((List<Point> points) -> points.size()).reversed());
        return resultats;
    }

    private void obtenirMovimentsAuxiliars(MoveNode moviment, List<Point> cami, List<List<Point>> resultats) {
        List<MoveNode> fills = moviment.getChildren();
        if (fills.isEmpty()) {
            // Afegir el camí al resultat si no hi ha més moviments possibles
            resultats.add(new ArrayList<>(cami));
            return;
        }

        for (MoveNode fill : fills) {
            cami.add(fill.getPoint());
            obtenirMovimentsAuxiliars(fill, cami, resultats);
            cami.remove(cami.size() - 1);  // Desfer l'últim moviment per explorar altres opcions
        }
    }

    private int evaluarEstat(GameStatus s) {
        nodesExplorats++;
        return evaluarEstatAux(s, jugadorMaxim) - evaluarEstatAux(s, jugadorMinim);
    }

    private int evaluarEstatAux(GameStatus s, PlayerType jugador) {
        nodesExplorats += 1;
        int heuristica = 0;
        int pawnPieces = 0;
        int kingPieces = 0;
        int backRowPieces = 0;
        int middleBoxPieces = 0;
        int middleRowPieces = 0;
        int vulnerablePieces = 0;
        int safePieces = 0;
        int opponentPieces = 0;

        for (int i = 0; i < s.getSize(); ++i) {
            for (int j = 0; j < s.getSize(); ++j) {
                CellType casilla = s.getPos(i, j);
                if (casilla.getPlayer() == jugador) {
                    if (casilla.isQueen()) {
                        kingPieces++; // Cuenta las reinas
                    } else {
                        pawnPieces++; // Cuenta las piezas regulares
                    }
                    if ((jugador == PlayerType.PLAYER1 && i == 0) || (jugador == PlayerType.PLAYER2 && i == s.getSize() - 1)) {
                        backRowPieces++; // Cuenta las piezas en la última fila
                    }
                    if (i >= s.getSize() / 2 - 1 && i <= s.getSize() / 2 && j >= s.getSize() / 2 - 1 && j <= s.getSize() / 2) {
                        middleBoxPieces++; // Cuenta las piezas en las 4 columnas del medio de las 2 filas del medio
                    } else if (i >= s.getSize() / 2 - 1 && i <= s.getSize() / 2) {
                        middleRowPieces++; // Cuenta las piezas en las 2 filas del medio pero no en las 4 columnas del medio
                    }
                    // Cuenta las piezas que pueden ser tomadas por el oponente en el próximo turno
                    if (i > 0 && j > 0 && i < s.getSize() - 1 && j < s.getSize() - 1) {
                        PlayerType opponentPlayer = (jugador == PlayerType.PLAYER1) ? PlayerType.PLAYER2 : PlayerType.PLAYER1;

                        if ((s.getPos(i + 1, j - 1).getPlayer() == opponentPlayer && s.getPos(i - 1, j + 1) == CellType.EMPTY)
                                || (s.getPos(i + 1, j + 1).getPlayer() == opponentPlayer && s.getPos(i - 1, j - 1) == CellType.EMPTY)) {
                            vulnerablePieces++;
                        } else {
                            safePieces++;
                        }
                    }

                } else if (casilla.getPlayer() != jugador) {
                    opponentPieces++;
                }
            }
        }

        // Ajusta la heurística según tus necesidades
        heuristica += pawnPieces * 5; // Añade el bonus que quieras para las piezas regulares
        heuristica += kingPieces * 7.75; // Añade el bonus que quieras para las reinas
        if (opponentPieces <= 3) {
            heuristica -= vulnerablePieces * 10; // Resta el bonus que quieras para las piezas que pueden ser tomadas por el oponente en el próximo turno

        } else {
            heuristica += pawnPieces * 5; // Añade el bonus que quieras para las piezas regulares
            heuristica += kingPieces * 7.75; // Añade el bonus que quieras para las reinas
            heuristica += backRowPieces * 4; // Añade el bonus que quieras para las piezas en la última fila
            heuristica += safePieces * 3; // Añade el bonus que quieras para las piezas que no pueden ser tomadas hasta que las piezas detrás de ella (o ella misma) se muevan
            heuristica -= vulnerablePieces * 3; // Resta el bonus que quieras para las piezas que pueden ser tomadas por el oponente en el próximo turno
            heuristica += middleBoxPieces * 2.5; // Añade el bonus que quieras para las piezas en las 4 columnas del medio de las 2 filas del medio
            heuristica += middleRowPieces * 0.5; // Añade el bonus que quieras para las piezas en las 2 filas del medio pero no en las 4 columnas del medio

        }

        return heuristica;
    }

    /**
     * Ens avisa que hem de parar la cerca en curs perquè s'ha exhaurit el temps
     * de joc.
     */
    @Override
    public String getName() {
        return name;
    }
}