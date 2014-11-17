package com.gitcolony.jenkins.gitcolony;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

public final class UrlProperty extends JobProperty<AbstractProject<?, ?>> {
  private String url;

  @DataBoundConstructor
  public UrlProperty(String url) { this.url = url; }
  public String getUrl() { return url; }

  @Extension
  public static final class DescriptorImpl extends JobPropertyDescriptor {

    public DescriptorImpl() { super(UrlProperty.class);  load(); }

    public boolean isApplicable(Class<? extends Job> jobType) {
      return AbstractProject.class.isAssignableFrom(jobType);
    }

    public String getDisplayName() { return "Gitcolony LiveBranch URL"; }

    @Override
    public JobProperty<?> newInstance(StaplerRequest req, JSONObject formData) throws FormException {
      UrlProperty tpp = req.bindJSON(UrlProperty.class, formData);
      return (tpp == null || tpp.url == null) ? null : tpp;
    }
  }
}
