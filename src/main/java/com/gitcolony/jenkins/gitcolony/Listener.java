package com.gitcolony.jenkins.gitcolony;

import hudson.model.TaskListener;
import hudson.model.Run;
import hudson.model.Job;
import hudson.model.Result;
import net.sf.json.JSONObject;

import java.net.URL;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.io.OutputStream;

@hudson.Extension
@SuppressWarnings("rawtypes")
public class Listener extends hudson.model.listeners.RunListener<Run> {
  public Listener() {
    super(Run.class);
  }

  @Override
  public void onCompleted(Run run, TaskListener listener) {
    // Load URL
    UrlProperty property = (UrlProperty) run.getParent().getProperty(UrlProperty.class);
    if (property == null) { return; }
    URL url;
    try {
      url = new URL(property.getUrl());
      if (!url.getProtocol().startsWith("http")) {
        throw new IllegalArgumentException("Not an http(s) url: " + property.getUrl());
      }
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException("Invalid url: " + property.getUrl());
    }

    // Build payload JSON
    JSONObject buildStatus = new JSONObject();
    Result  result = run.getResult();
    buildStatus.put("status", result != null ? result.toString() : "UNKNOWN");
    String sha = null;
    try { sha = run.getEnvironment(listener).get("GIT_COMMIT"); } catch (Exception e) {}
    buildStatus.put("sha", sha);

    // Send notification
    try {
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("POST");
      conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
      //conn.setConnectTimeout(timeout);
      //conn.setReadTimeout(timeout);

      conn.setDoOutput(true);
      OutputStream output = conn.getOutputStream();
      output.write(buildStatus.toString().getBytes("UTF-8"));
      output.close();

      int code = conn.getResponseCode();
      listener.getLogger().println(String.format("Gitcolony notification sent: %d", code));

    } catch (Throwable error) {

      error.printStackTrace(listener.error("Gitcolony notification failed"));
      listener.getLogger().println(String.format("Gitcolony notification failed - %s: %s",
                                                 error.getClass().getName(), error.getMessage()));
    }
  }
}