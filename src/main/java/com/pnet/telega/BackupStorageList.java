package com.pnet.telega;

import com.pnet.util.PersistentDataService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;

//TODO: load, save on operations, inject messages
@RequiredArgsConstructor
public class BackupStorageList<T> {
    @Getter
    private final String path;
    private ArrayList<T> list;

    public void load() {
        list = PersistentDataService.loadObject(path, list.getClass());
    }

    private void save() {
        PersistentDataService.saveObject(path, list);
    }

    public boolean add(T t) {
        if(null==t)
            return false;
        boolean add = list.add(t);
        save();
        return add;
    }


    public boolean remove(Object o) {
        boolean remove = list.remove(o);
        save();
        return remove;
    }

    public T peek(){
        if(list.size()>0)
            return list.get(0);
        return null;
    }
}
