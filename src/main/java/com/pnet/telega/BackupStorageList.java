package com.pnet.telega;

import com.pnet.util.PersistentDataService;
import lombok.*;

import java.io.IOException;
import java.util.ArrayList;

//TODO: load, save on operations, inject messages
@RequiredArgsConstructor
public class BackupStorageList<T> {
    @Getter
    private final String path;
    private ArrayList<T> list;

    public void load() throws IOException {
        ListHolder arrayListListHolder = new ListHolder();
        if(PersistentDataService.resourceExists(path)) {
            arrayListListHolder = PersistentDataService.loadObject(path, arrayListListHolder.getClass());
            list = arrayListListHolder.list;
        }else
            list = new ArrayList<>();
    }

    private void save() {
        try {
            ListHolder o = new ListHolder();
            o.list=list;
            PersistentDataService.saveObject(path, o);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
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

    //@AllArgsConstructor
    @NoArgsConstructor
    @Data
    public class ListHolder {
        public ArrayList<T> list;
    }
}
