import SwiftUI

struct ContentView: View {
    var body: some View {
        VStack(spacing: 16) {
            Text("AetherQuest iOS")
                .font(.largeTitle)
                .bold()
            Text("KMP shared core scaffold ready")
                .foregroundStyle(.secondary)
            Text("Connect shared framework and gameplay screens in next step.")
                .font(.footnote)
                .multilineTextAlignment(.center)
        }
        .padding(24)
    }
}
