package org.nfctools.examples.hce;

/**
 * Created by kevin on 05/02/14.
 */
public interface IOnData {
    public void onUUId(String uuid);
    public void onUserToken(String token);
    public void onError(String error);
}
