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
    private final boolean ignoreUnknownProperties;

    public void load() throws IOException {
        PersistentImage arrayListPersistentImage = new PersistentImage();
        if(PersistentDataService.resourceExists(path)) {
            list=PersistentDataService.loadObject(path, new ArrayList<>().getClass(),
                    ignoreUnknownProperties);
            //arrayListPersistentImage = PersistentDataService.loadObject(path, arrayListPersistentImage.getClass());
            //list = arrayListPersistentImage.list;
        }else
            list = new ArrayList<>();
    }

    private void save() {
        try {
            PersistentImage o = new PersistentImage();
            o.list=list;
            PersistentDataService.saveObject(path, list);
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
    public class PersistentImage {
        public ArrayList<T> list;
    }
}
