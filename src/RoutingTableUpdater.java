import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class RoutingTableUpdater implements Runnable {

    boolean start;
    int TIMER = 5000;

    public RoutingTableUpdater() {
        this.start = true;
    }

    public void ReadRouteFileAndUpdateRouteTable() {
        try {
            List<String> routeEntries = Files.readAllLines(Paths.get("src/RouteTable/RoutingTable.txt"));
            for (String route : routeEntries) {
                String[] entries = route.split("\t\t");
                NextHopInfoTable nextHopInfo;
                nextHopInfo = new NextHopInfoTable(InetAddress.getByName(entries[1].split("/")[1]),
                        InetAddress.getByName(entries[2].split("/")[1]), Integer.getInteger(entries[3]));

                synchronized (RoutingTable.routeEntries) {
                    LunarRover.routingTable.updateRoutingTable(InetAddress.getByName(entries[0].split("/")[1]), nextHopInfo);
                    if (!LunarRover.neighbors.contains(InetAddress.getByName(entries[2].split("/")[1])))
                        LunarRover.neighbors.add(InetAddress.getByName(entries[2].split("/")[1]));
                }
            }
        } catch (IOException e) {
            System.err.println("Routing table file not found.");
        }
    }

    public void ReadRouteTableAndUpdateRouteFile() {

    }

    @Override
    public void run() {
        while (true) {
            // first time launch
            if (this.start) {
                ReadRouteFileAndUpdateRouteTable();
                this.start = false;
            } else {
                ReadRouteTableAndUpdateRouteFile();
            }
            // after update, wait for 5 seconds.
            try {
                Thread.sleep(this.TIMER);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        RoutingTable rt = new RoutingTable();
        LunarRover lr = new LunarRover();
        RoutingTableUpdater rtu = new RoutingTableUpdater();
//        System.out.println(Paths.get("src/RouteTable/RoutingTable.txt").toAbsolutePath());
        rtu.ReadRouteFileAndUpdateRouteTable();
    }
}
