source = ["./hpcviewer.app"]
bundle_id = "edu.rice.cs.hpcviewer"

apple_id {
  username = "la5@rice.edu"
  password = "@env:AC_PASSWORD"
}

sign {
  application_identity = "Developer ID Application: Laksono Adhianto"
  entitlements_file = "p.plist"
}

dmg {
  output_path = "hpcviewer.dmg"
  volume_name = "hpcviewer"
}
