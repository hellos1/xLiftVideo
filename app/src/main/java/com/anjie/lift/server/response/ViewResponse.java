package com.anjie.lift.server.response;

public class ViewResponse extends ServerResponse
{
    public int verson;

    private String downloadUrl;

    public ViewResponse()
    {

    }

    public int getViewVersion()
    {
        return verson;
    }

    public String getDownloadUrl()
    {
        return downloadUrl;
    }
}
