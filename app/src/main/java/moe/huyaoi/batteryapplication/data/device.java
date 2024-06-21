package moe.huyaoi.batteryapplication.data;

import moe.huyaoi.batteryapplication.dataTypes.DeviceItem;
import moe.huyaoi.batteryapplication.event.TextEvent;
import moe.huyaoi.batteryapplication.event.TextEventSource;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class device {
    private static final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private static ArrayList<DeviceItem> datal = new ArrayList<>();//存储界面
    private static final long EXPIRATION_TIME = 15 * 60 * 1000; // 15分钟
    public static int Size(){
        return datal.size();
    }
    public static TextEventSource eventSource = new TextEventSource();

    public static DeviceItem Get(String ID){
        rwl.readLock().lock();
        for (DeviceItem item: datal) {
            if(Objects.equals(ID, item.ID)){
                rwl.readLock().unlock();
                return item;
            }
        }
        rwl.readLock().unlock();
        return null;
    }

    private static void Set(DeviceItem di){
        int count = 0;
        boolean yes = false;
        for (DeviceItem item: datal) {
            if(Objects.equals(di.ID, item.ID)){
                yes = true;
                di.IsTcpConnect = item.IsTcpConnect;
                di.IsUdpConnect = item.IsUdpConnect;
                datal.set(count,di);
                break;
            }
            count++;
        }
        if(!yes){
            datal.add(di);
        }
    }

    public static void SetList(ArrayList<DeviceItem> di){
        rwl.writeLock().lock();
        for (DeviceItem item: di) {
            Set(item);
        }
        rwl.writeLock().unlock();
        eventSource.triggerEvent(new TextEvent("-"));
    }

    public static void SetOne(DeviceItem di){
        rwl.writeLock().lock();
        Set(di);
        rwl.writeLock().unlock();
        eventSource.triggerEvent(new TextEvent("-"));
    }

    public static void Remove(String ID,boolean isTcp){
        rwl.writeLock().lock();
        int count = 0;
        for (DeviceItem item: datal) {
            if(Objects.equals(ID, item.ID)){
                if(isTcp){
                    item.IsTcpConnect = false;
                }else{
                    item.IsUdpConnect = false;
                }
                if(!item.IsTcpConnect && !item.IsUdpConnect){
                    datal.remove(count);
                }else{
                    datal.set(count,item);
                }
                break;
            }
            count++;
        }
        rwl.writeLock().unlock();
    }

    private static void removeExpiredItems() {
        long now = System.currentTimeMillis();
        datal.removeIf(item -> now - item.LastUpdateTime > EXPIRATION_TIME);
    }

    public static void publicRemoveExpiredItems() {
        rwl.writeLock().lock();
        long now = System.currentTimeMillis();
        datal.removeIf(item -> now - item.LastUpdateTime > EXPIRATION_TIME);
        rwl.writeLock().unlock();
    }

    public static ArrayList<DeviceItem> CloneArray(){
        rwl.writeLock().lock();
        removeExpiredItems();
        ArrayList<DeviceItem> result = new ArrayList<>(datal);
        rwl.writeLock().unlock();
        return result;
    }
}
