const admin = require("firebase-admin");
admin.initializeApp({ credential: admin.credential.cert(require("./serviceAccount.json")) });
const db = admin.firestore();
async function deleteAll() {
  const collections = ["listings", "motors", "agents", "rooms", "offplan"];
  for (const col of collections) {
    const snap = await db.collection(col).get();
    if (snap.empty) { console.log(col + ": empty"); continue; }
    const batch = db.batch();
    snap.docs.forEach(doc => batch.delete(doc.ref));
    await batch.commit();
    console.log("Deleted " + snap.size + " from " + col);
  }
  console.log("All done"); process.exit(0);
}
deleteAll().catch(e => { console.error(e); process.exit(1); });
