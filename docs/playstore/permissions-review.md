# Permissions Review

## Current Android Manifest
- No dangerous runtime permissions requested

## Policy Targets
- Request only strictly required permissions
- Keep offline mode as default behavior
- If analytics/ads added, update privacy policy and disclosures

## Data Safety Form Draft
- Data collected: none in current offline build
- Data shared: none
- Encryption in transit: N/A for offline mode

## Checklist
- [ ] Re-check merged manifest at release time
- [ ] Verify no accidental SDK permission additions
- [ ] Update Data Safety section if backend is introduced
