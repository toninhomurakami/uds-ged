package br.com.uds.tools.ged.service;

/**
 * Resultado de download: conteúdo do arquivo e nome sugerido para salvar (preserva nome original e extensão).
 */
public record FileDownload(byte[] content, String filename) {}
