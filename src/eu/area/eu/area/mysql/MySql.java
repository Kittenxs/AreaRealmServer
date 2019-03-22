package eu.area.eu.area.mysql;

import eu.area.kernel.Main;
import eu.area.objects.Compte;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Meow on 2017-01-24.
 */
public class MySql {
    public static Connection createConnection() throws SQLException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        // Informations
        String host = Main.getConfig().getDb_host();
        String name = Main.getConfig().getDb_name();
        String user = Main.getConfig().getDb_user();
        String pass = Main.getConfig().getDb_passord();
        //Préferable de gerer l'auto-reconnect toi meme
        //Parfois tu peux avoir des soucis/bugs qui peuvennt pop sneaky sur ton app qui & ta db est indispo' pour une raison X /Y & quand elle revient par magie tu peux obtenir des resultats plutot inattendu.
        //C'est pour cette raison que beaucoup gèrent les db commits/rollback eux meme 
        //Hesite pas si tu need + de détails dessus. Pm Discord sur profile github
        return DriverManager.getConnection("jdbc:mysql://" + host + "/" + name + "?user=" + user + "&password=" + pass +"&useUnicode=true&characterEncoding=UTF-8&failOverReadOnly=false");
    }

    public static Compte getAccountByName(String name) {
        Compte compte = null;
        try {
            Connection co = createConnection();
            String query = "SELECT vip, m_password, m_reg, banned, banned_time, pass, saltKey, logged, pseudo, question, guid, level FROM accounts WHERE account LIKE ? LIMIT 1;";
            PreparedStatement ps = co.prepareStatement(query);
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                compte = new Compte(rs.getString("pass"), rs.getString("saltKey"), rs.getInt("m_password"), rs.getInt("banned"), rs.getInt("logged"), rs.getString("pseudo"), rs.getString("question"), rs.getInt("guid"), name, rs.getInt("level"), rs.getInt("banned_time"), rs.getInt("vip"), rs.getInt("m_reg"));
            }
            rs.close();
            ps.close();
            co.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (compte != null) {
            List<String> ipAllowed = new ArrayList<String>();
            try {
                Connection co = createConnection();
                //Pour ton code de validation, j'suis assez curieux ! Peut il être numerique ?
                //Admettons que chaque Ips =  222.222.222.222  + Ok
                //La table peut facilement devenir très lourde sa ferait 16bytes/record !
                String query = "SELECT * FROM ipProtege WHERE compte = ? AND codeValidation = 'ok';";
                PreparedStatement ps = co.prepareStatement(query);
                ps.setInt(1, compte.getGuid());
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    ipAllowed.add(rs.getString("ip"));
                }
                rs.close();
                ps.close();
                co.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            compte.setIpAllowed(ipAllowed);
        }
        return compte;
    }

    public static boolean pseudoUsed(String pseudo) {
        boolean used = false;
        try {
            Connection co = createConnection();
            String query = "SELECT pseudo FROM accounts WHERE pseudo LIKE ?;";
            PreparedStatement ps = co.prepareStatement(query);
            ps.setString(1, pseudo);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                used = true;
            }
            rs.close();
            ps.close();
            co.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return used;
    }

    public static void saveAccount(Compte compte) {
        try {
            Connection co = createConnection();
            String query = "UPDATE accounts SET pseudo = ? WHERE guid = ?;";
            PreparedStatement ps = co.prepareStatement(query);
            ps.setString(1, compte.getPseudo());
            ps.setInt(2, compte.getGuid());
            ps.execute();
            ps.close();
            co.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void unbanAccount(int guid) {
        try {
            Connection co = createConnection();
            String query = "UPDATE accounts SET banned_time = 0, banned = 0 WHERE guid = ?;";
            PreparedStatement ps = co.prepareStatement(query);
            ps.setInt(1, guid);
            ps.execute();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
