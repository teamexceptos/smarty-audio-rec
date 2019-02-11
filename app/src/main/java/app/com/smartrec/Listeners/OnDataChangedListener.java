package app.com.smartrec.Listeners;

import java.util.List;

public interface OnDataChangedListener<T> {

    public void onListChanged(List<T> list);

}
