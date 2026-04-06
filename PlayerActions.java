package app;

public class PlayerActions {

    //player matches the current bet
    public static boolean call(Player player, double amount) {
        if (amount <= 0) return false;
        if (player.getChipBalance() >= amount) {
            player.adjustChips(-amount);
            System.out.println(player.getName() + " calls with " + amount + " chips. Remaining: " + player.getChipBalance());
            return true;
        } else {
            System.out.println(player.getName() + " cannot call. Insufficient chips.");
            return false;
        }
    }

    //player puts in chips to start a new bet
    public static boolean bet(Player player, double amount) {
        if (amount <= 0) return false;
        if (player.getChipBalance() >= amount) {
            player.adjustChips(-amount);
            System.out.println(player.getName() + " bets " + amount + " chips. Remaining: " + player.getChipBalance());
            return true;
        } else {
            System.out.println(player.getName() + " cannot bet. Insufficient chips.");
            return false;
        }
    }

    //player raises the current bet by a specified amount
    public static boolean raise(Player player, double amount) {
        if (amount <= 0) return false;
        if (player.getChipBalance() >= amount) {
            player.adjustChips(-amount);
            System.out.println(player.getName() + " raises by " + amount + " chips. Remaining: " + player.getChipBalance());
            return true;
        } else {
            System.out.println(player.getName() + " cannot raise. Insufficient chips.");
            return false;
        }
    }

    //player checks (does not bet but stays in the hand)
    public static void check(Player player) {
        System.out.println(player.getName() + " checks.");
    }

    //player folds and is out of the current hand
    public static void fold(Player player) {
        player.fold();
    }
}