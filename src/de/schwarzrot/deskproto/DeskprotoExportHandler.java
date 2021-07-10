package de.schwarzrot.deskproto;


/*
 * **************************************************************************
 *
 *  file:       LinuxCNCExportHandler.java
 *  project:    GUI for linuxcnc
 *  subproject: exporthandler for Deskproto
 *  purpose:    exporthandler that writes tool-files in format used
 *  created:    4.12.2020 by Django Reinhard
 *  copyright:  all rights reserved
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * **************************************************************************
 */
import java.io.File;
import java.io.PrintWriter;

import de.schwarzrot.linuxcnc.data.CategoryInfo;
import de.schwarzrot.linuxcnc.data.LibInfo;
import de.schwarzrot.linuxcnc.data.ToolInfo;
import de.schwarzrot.linuxcnc.export.IExportHandler;


/*
 * Deskproto uses a single file for each tool
 */
public class DeskprotoExportHandler implements IExportHandler {
   @Override
   public void closeCategory(CategoryInfo catInfo) throws Exception {
      cat = null;
   }


   @Override
   public void closeLibrary(LibInfo libInfo) throws Exception {
      lib = null;
   }


   @Override
   public void closeTool(ToolInfo toolInfo) throws Exception {
   }


   @Override
   public void openCategory(CategoryInfo catInfo) throws Exception {
      if (baseDir != null)
         cat = catInfo;
   }


   @Override
   public void openLibrary(LibInfo libInfo, String fileName) throws Exception {
      File dpd = determineReleaseDir();

      if (dpd != null && dpd.isDirectory()) {
         baseDir = new File(dpd, "Drivers");
         if (!baseDir.exists() || !baseDir.isDirectory())
            baseDir = null;
      }
      if (baseDir != null) {
         lib        = libInfo;
         toolNumber = 0;
      }
   }


   @Override
   public void openTool(ToolInfo toolInfo) throws Exception {
      if (baseDir == null)
         return;
      File        fn = new File(baseDir,
            createToolFileName(cat.getName(), toolInfo.getCuttingRadius(), toolInfo.getCuttingLength()));
      PrintWriter pw = new PrintWriter(fn);

      pw.println("[DeskProto Cutter]");
      pw.print("Version = ");
      pw.println(dpRelease);
      pw.print("Name = ");
      pw.println(toolInfo.getToolName());
      pw.print("Type = ");
      pw.println(cat.getType());
      pw.print("FreeLength = ");
      pw.println(toolInfo.getFreeLength());
      pw.print("ShaftDiameter = ");
      pw.println(toolInfo.getShankDiameter());
      pw.print("SlopeAngle = ");
      pw.println(toolInfo.getSlopeAngle());
      pw.print("FluteLength = ");
      pw.println(toolInfo.getFluteLength());
      pw.print("FluteDiameter = ");
      pw.println(toolInfo.getFluteDiameter());
      pw.print("CuttingLength = ");
      pw.println(toolInfo.getCuttingLength());
      pw.print("TipDiameter = ");
      pw.println(toolInfo.getTipDiameter());
      pw.print("CuttingAngle = ");
      pw.println(toolInfo.getCuttingAngle());
      pw.print("IsMultipleDiameter = ");
      pw.println(toolInfo.getShankDiameter() != toolInfo.getFluteDiameter() ? "1" : "0");
      pw.print("MaximumSpindleSpeed = ");
      pw.println(30000);
      pw.print("NumberInMachine = ");
      pw.println(++toolNumber);
      pw.print("UseAutoSpeeds = ");
      pw.println(0);
      pw.print("AutoFeedrate = ");
      pw.println("0.000");
      pw.print("AutoSpindlespeed = ");
      pw.println(0);
      pw.flush();
      pw.close();
   }


   protected String createToolFileName(String name, double cutRadius, double cutLen) {
      StringBuilder sb  = new StringBuilder(name);
      String        tmp = Double.toString(cutRadius);

      sb.append("r").append(tmp.replace(".", "p"));
      tmp = Double.toString(cutLen);
      sb.append("l").append(tmp.replace(".", "p"));
      sb.append(".ctr");

      return sb.toString();
   }


   protected File determineReleaseDir() {
      File   base   = new File(System.getProperty("user.home"),
            ".local/share/Delft Spline Systems/DeskProto/");
      File   cfgDir = null;
      double ov     = 0;
      double v      = 0;

      for (File f : base.listFiles()) {
         v = 0;
         if (f.isDirectory()) {
            try {
               v = Double.parseDouble(f.getName());
            } catch (Exception e) {
            }
            if (v != 0 && v > ov) {
               cfgDir = f.getAbsoluteFile();
            }
         }
      }
      dpRelease = v;

      return cfgDir;
   }


   private File         baseDir;
   private LibInfo      lib;
   private CategoryInfo cat;
   private int          toolNumber;
   private double       dpRelease;
}
