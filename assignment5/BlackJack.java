import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BlackJack {
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        BlackJack game = new BlackJack();
        game.play();
        long endTime = System.currentTimeMillis();
        long elapsedTimeInMillis = endTime - startTime;
        System.out.println("실행 시간(ms): " + elapsedTimeInMillis);
    }

    public static List<Integer> dealersWins = new ArrayList<>();
    public static List<Integer> playersWins = new ArrayList<>();
    public static List<String> winRate = new ArrayList<>();
    private List<Integer> deck;
    private int numCards;
    private static int dealerWins;
    private static int playerWins;

    private int[][] dealerMemo;
    private int[][] playerMemo;

    public BlackJack() {
        dealerWins = 0;
        playerWins = 0;
        resetGame();
        dealerMemo = new int[53][23];
        playerMemo = new int[53][23];
        for (int i = 0; i < 53; i++) {
            for (int j = 0; j < 23; j++) {
                dealerMemo[i][j] = -1;
                playerMemo[i][j] = -1;
            }
        }
    }

    private void resetGame() {
        numCards = 52;
        deck = new ArrayList<>();
        for (int i = 1; i <= 13; i++) {
            for (int j = 0; j < 4; j++) {
                deck.add(i);
            }
        }
        Collections.shuffle(deck); // 덱을 랜덤으로 섞기
    }

    public void play() {
        while (numCards > 4) {
            int dealerInitial1 = drawCard();
            int dealerInitial2 = drawCard();
            int playerInitial1 = drawCard();
            int playerInitial2 = drawCard();

            int dealerResult = dealerDP(dealerInitial1 + dealerInitial2, numCards);
            int playerResult = playerDP(playerInitial1 + playerInitial2, numCards);

            if (playerResult > 21) {
                dealerWins++;
            } else if (dealerResult > 21) {
                playerWins++;
            } else {
                if (dealerResult >= playerResult) {
                    dealerWins++;
                } else {
                    playerWins++;
                }
            }

            // 남은 카드가 4장 이하일 때 게임 종료
            if (numCards <= 4) {
                break;
            }
        }

        System.out.println("Final Scores:");
        System.out.println("Dealer Wins: " + dealerWins);
        System.out.println("Player Wins: " + playerWins);
    }

    private int dealerDP(int currentSum, int cardsLeft) {
        if (currentSum > 21) return 22;
        if (currentSum >= 17) return currentSum;
        if (dealerMemo[cardsLeft][currentSum] != -1) return dealerMemo[cardsLeft][currentSum];

        int bestScore = currentSum;
        List<Integer> tempDeck = new ArrayList<>(deck);
        for (int i = 0; i < tempDeck.size(); i++) {
            int cardValue = getCardValue(tempDeck.get(i));
            deck.remove(tempDeck.get(i));
            numCards--;
            bestScore = Math.max(bestScore, dealerDP(currentSum + cardValue, numCards));
            deck.add(tempDeck.get(i));
            numCards++;
        }
        dealerMemo[cardsLeft][currentSum] = bestScore;
        return bestScore;
    }

    private int playerDP(int currentSum, int cardsLeft) {
        if (currentSum > 21) return 22;
        if (currentSum >= 20) return currentSum;
        if (playerMemo[cardsLeft][currentSum] != -1) return playerMemo[cardsLeft][currentSum];

        int bestScore = currentSum;
        List<Integer> tempDeck = new ArrayList<>(deck);
        for (int i = 0; i < tempDeck.size(); i++) {
            int cardValue = getCardValue(tempDeck.get(i));

            // peek 카드를 보고 결정
            int peekedCardValue = cardValue;
            if (currentSum + peekedCardValue <= 21) {
                deck.remove(tempDeck.get(i));
                numCards--;
                bestScore = Math.max(bestScore, playerDP(currentSum + cardValue, numCards));
                deck.add(tempDeck.get(i));
                numCards++;
            }
        }
        playerMemo[cardsLeft][currentSum] = bestScore;
        return bestScore;
    }

    private int getCardValue(int cardIndex) {
        if (cardIndex >= 2 && cardIndex <= 10) return cardIndex;
        if (cardIndex == 1) return 10;  // A는 10으로 처리
        if (cardIndex == 11) return 11; // J는 11으로 처리
        if (cardIndex == 12) return 12; // Q는 12로 처리
        return 13;                      // K는 13으로 처리
    }

    private int drawCard() {
        int card = deck.remove(deck.size() - 1);
        numCards--;
        return getCardValue(card);
    }
}
