package org.obiba.runtime;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a version number and allows comparing to other version numbers.
 * <p/>
 * Format is <code>major'.'minor('.'micro)?(('.'|'-'|'_')qualifier)?</code> <br/>
 * where major, minor and micro are composed of digits and qualifier is an arbitrary string.
 * 
 * @author plaflamm
 * 
 */
final public class Version implements Comparable<Version> {

  private static final Pattern VERSION_PATTERN = Pattern.compile("(\\d+)\\.(\\d+)(\\.(\\d+))?(([\\.\\-_])(\\D.+))?");

  private final int major;

  private final int minor;

  private final int micro;

  private final String qualifier;

  public Version(int major, int minor) {
    this(major, minor, 0, null);
  }

  public Version(int major, int minor, int micro) {
    this(major, minor, micro, null);
  }

  public Version(int major, int minor, int micro, String qualifier) {
    this.major = major;
    this.minor = minor;
    this.micro = micro;
    if(qualifier == null) {
      this.qualifier = "";
    } else {
      this.qualifier = qualifier;
    }
  }

  public Version(String version) {
    try {
      Matcher m = VERSION_PATTERN.matcher(version);
      if(m.matches() == false) {
        throw invalidVersionString(version, "cannot parse version.");
      }
      String major = m.group(1);
      String minor = m.group(2);
      String micro = m.group(4);
      String qualifier = m.group(7);
      if(major == null || minor == null) {
        throw invalidVersionString(version, "major and minor version required");
      }
      this.major = Integer.parseInt(major);
      this.minor = Integer.parseInt(minor);
      if(micro != null) {
        this.micro = Integer.parseInt(micro);
      } else {
        this.micro = 0;
      }
      if(qualifier != null) {
        this.qualifier = qualifier;
      } else {
        this.qualifier = "";
      }
    } catch(RuntimeException e) {
      throw invalidVersionString(version, e);
    }
  }

  public int compareTo(Version rhs) {
    if(major != rhs.major) return major - rhs.major;
    if(minor != rhs.minor) return minor - rhs.minor;
    if(micro != rhs.micro) return micro - rhs.micro;
    return qualifier.compareTo(rhs.qualifier);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder().append(major).append('.').append(minor).append('.').append(micro);
    if(qualifier != null && qualifier.length() > 0) sb.append('-').append(qualifier);
    return sb.toString();
  }

  @Override
  public boolean equals(Object obj) {
    if(this == obj) {
      return true;
    }
    if(obj == null) {
      return false;
    }
    if(getClass() != obj.getClass()) {
      return false;
    }

    final Version rhs = (Version) obj;
    if(major != rhs.major) {
      return false;
    }
    if(minor != rhs.minor) {
      return false;
    }
    if(micro != rhs.micro) {
      return false;
    }
    return qualifier.equals(rhs.qualifier);
  }

  @Override
  public int hashCode() {
    final int PRIME = 31;
    int hash = 1;
    hash = PRIME * hash + major;
    hash = PRIME * hash + minor;
    hash = PRIME * hash + micro;
    hash = PRIME * hash + (qualifier != null ? qualifier.hashCode() : 0);
    return hash;
  }

  public int getMajor() {
    return major;
  }

  public int getMinor() {
    return minor;
  }

  public int getMicro() {
    return micro;
  }

  public String getQualifier() {
    return qualifier;
  }

  private IllegalArgumentException invalidVersionString(String version, String reason) {
    return new IllegalArgumentException("Invalid version string '" + version + "'. Expected format is \"major'.'minor('.'micro)?(('.'|'-'|'_')qualifier)?\": " + reason);
  }

  private IllegalArgumentException invalidVersionString(String version, Exception e) {
    return new IllegalArgumentException("Invalid version string '" + version + "'. Expected format is \"major'.'minor('.'micro)?(('.'|'-'|'_')qualifier)?\"", e);
  }

}
