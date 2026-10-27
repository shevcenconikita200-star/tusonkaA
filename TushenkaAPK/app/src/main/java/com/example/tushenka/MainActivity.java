pendingBackupName = (fileName == null || fileName.trim().isEmpty())
                        ? "tushenka-backup.json" : fileName;
                if (!pendingBackupName.toLowerCase().endsWith(".json")) {
                    pendingBackupName += ".json";
                }
                pendingBackupJson = json == null ? "{}" : json;

                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("application/json");
                intent.putExtra(Intent.EXTRA_TITLE, pendingBackupName);

                try {
                    startActivityForResult(intent, CREATE_BACKUP_REQUEST);
                } catch (Exception e) {
                    pendingBackupJson = null;
                    Toast.makeText(MainActivity.this,
                            "Не удалось открыть сохранение файла", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private boolean openExternal(String url) {
        Uri uri = Uri.parse(url);
        String scheme = uri.getScheme();

        if (scheme == null) return false;

        if (scheme.equals("file") ||
            scheme.equals("javascript") ||
            scheme.equals("about") ||
            scheme.equals("blob") ||
            scheme.equals("data")) {
            return false;
        }

        if (scheme.equals("http") ||
            scheme.equals("https") ||
            scheme.equals("tel") ||
            scheme.equals("geo") ||
            scheme.equals("google.navigation") ||
            scheme.equals("market")) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                return true;
            } catch (Exception e) {
                return true;
            }
        }

        if (scheme.equals("intent")) {
            try {
                Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                startActivity(intent);
            } catch (Exception ignored) {
            }
            return true;
        }

        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILE_CHOOSER_REQUEST) {
            if (filePathCallback == null) return;

            Uri[] results = null;
            if (resultCode == Activity.RESULT_OK && data != null
                    && data.getData() != null) {
                results = new Uri[]{data.getData()};
            }
            filePathCallback.onReceiveValue(results);
            filePathCallback = null;
            return;
        }

        if (requestCode == CREATE_BACKUP_REQUEST) {
            if (resultCode == Activity.RESULT_OK && data != null
                    && data.getData() != null && pendingBackupJson != null) {
                try (OutputStream output =
                             getContentResolver().openOutputStream(data.getData())) {
                    if (output == null) throw new IOException("Не удалось открыть файл");
                    output.write(pendingBackupJson.getBytes("UTF-8"));
                    output.flush();
                    Toast.makeText(this, "Резервная копия сохранена",
                            Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(this, "Ошибка сохранения: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            }
            pendingBackupJson = null;
            pendingBackupName = null;
        }
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (filePathCallback != null) {
            filePathCallback.onReceiveValue(null);
            filePathCallback = null;
        }
        if (webView != null) {
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}
